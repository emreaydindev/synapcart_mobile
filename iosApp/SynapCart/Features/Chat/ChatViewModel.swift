import Foundation
import Observation
import Shared

/// One row of the conversation (Kotlin `ChatMessage` mapped to a Swift value).
struct ChatItem: Identifiable, Equatable {
    let id: String
    let text: String
    let isUser: Bool
    let products: [ProductDTO]

    static func == (lhs: ChatItem, rhs: ChatItem) -> Bool { lhs.id == rhs.id && lhs.text == rhs.text }
}

/// The dashboard: sessions, messages and the streamed agent answer (mirrors Android's DashboardViewModel).
@MainActor
@Observable
final class ChatViewModel {
    private(set) var sessions: [SessionResponse] = []
    private(set) var currentSessionId: Int32?
    private(set) var items: [ChatItem] = []
    /// In-flight agent status (routing / searching / thinking); nil when idle.
    private(set) var agentStatus: String?
    private(set) var pendingProducts: [ProductDTO] = []
    private(set) var isLoadingMessages = false
    private(set) var quota: (left: Int, limit: Int)?
    var input = ""

    private let app: AppState
    private var stream: WatchHandle?

    init(app: AppState) {
        self.app = app
    }

    var isStreaming: Bool { agentStatus != nil }

    var currentTitle: String {
        sessions.first { $0.id == currentSessionId }?.title ?? L("chat_new")
    }

    func onAppear() async {
        await loadSessions()
        await refreshQuota()
    }

    func loadSessions() async {
        do {
            sessions = try await app.client.sessions()
        } catch {
            app.handle(error, fallback: "error_history_load")
        }
    }

    func refreshQuota() async {
        guard let profile = try? await app.client.profile() else { return }
        if !profile.isPremium, let used = profile.usageToday?.intValue, let limit = profile.dailyLimit?.intValue {
            quota = (left: max(limit - used, 0), limit: limit)
        } else {
            quota = nil
        }
    }

    func newChat() {
        stream?.cancel()
        agentStatus = nil
        currentSessionId = nil
        items = []
        pendingProducts = []
    }

    func open(_ session: SessionResponse) async {
        if session.locked?.boolValue == true {
            app.showPaywall = true
            return
        }
        stream?.cancel()
        agentStatus = nil
        currentSessionId = session.id
        isLoadingMessages = true
        defer { isLoadingMessages = false }
        do {
            let messages = try await app.client.messages(sessionId: session.id)
            items = messages.map { ChatItem(id: $0.id, text: $0.text, isUser: $0.isUser, products: $0.products) }
        } catch {
            items = []
            app.handle(error, fallback: "error_messages_load")
        }
    }

    func rename(_ session: SessionResponse, to title: String) async {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        do {
            _ = try await app.client.renameSession(id: session.id, title: trimmed)
            await loadSessions()
        } catch {
            app.handle(error, fallback: "error_chat_rename")
        }
    }

    func delete(_ session: SessionResponse) async {
        do {
            _ = try await app.client.deleteSession(id: session.id)
            if session.id == currentSessionId { newChat() }
            await loadSessions()
        } catch {
            app.handle(error, fallback: "error_chat_delete")
        }
    }

    func send(_ text: String? = nil) async {
        let message = (text ?? input).trimmingCharacters(in: .whitespacesAndNewlines)
        guard !message.isEmpty, !isStreaming else { return }
        input = ""

        var sessionId = currentSessionId
        if sessionId == nil {
            do {
                let session = try await app.client.createSession()
                sessionId = session.id
                currentSessionId = session.id
            } catch {
                app.handle(error, fallback: "error_chat_start")
                input = message
                return
            }
        }
        guard let sessionId else { return }

        items.append(ChatItem(id: UUID().uuidString, text: message, isUser: true, products: []))
        agentStatus = "routing"
        pendingProducts = []

        stream = app.client.streamAgent(
            sessionId: sessionId,
            message: message,
            onEvent: { [weak self] event in self?.handle(event) },
            onComplete: { [weak self] error in self?.finish(error) }
        )
    }

    private func handle(_ event: AgentStreamEvent) {
        let status = event.status ?? ""
        switch status {
        case "routing", "searching":
            agentStatus = status
        case "thinking":
            agentStatus = status
            pendingProducts = event.products ?? []
        case "completed", "out_of_scope", "error":
            let products = status == "completed" ? (event.products ?? pendingProducts) : []
            items.append(ChatItem(id: UUID().uuidString, text: event.analysis ?? L("error_try_again"), isUser: false, products: products))
            agentStatus = nil
            pendingProducts = []
        default:
            break
        }
    }

    private func finish(_ error: ApiException?) {
        agentStatus = nil
        pendingProducts = []
        if let error {
            if error is ApiException.QuotaExceeded && !app.isPremium {
                app.showPaywall = true
            }
            app.show(error.userMessage(fallback: "error_try_again"), isError: true)
        }
        Task {
            await loadSessions()
            await refreshQuota()
        }
    }

    /// Sessions grouped by relative day for the drawer, newest first.
    var groupedSessions: [(group: Formatters.SessionGroup, sessions: [SessionResponse])] {
        let grouped = Dictionary(grouping: sessions) { Formatters.sessionGroup(Formatters.timestamp($0.createdAt)) }
        return Formatters.SessionGroup.allCases.compactMap { group in
            grouped[group].map { (group, $0) }
        }
    }
}
