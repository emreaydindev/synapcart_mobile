import Shared
import SwiftUI

enum MainRoute: Hashable {
    case product(String)
    case favorites
    case profile
    case compare
}

/// Holds products handed between screens (Android's SelectedProductStore).
@MainActor
@Observable
final class ProductStore {
    private var products: [String: ProductDTO] = [:]
    var siblings: [ProductDTO] = []
    var compareProducts: [ProductDTO] = []

    func put(_ product: ProductDTO, siblings: [ProductDTO] = []) -> MainRoute {
        products[product.stableID] = product
        self.siblings = siblings
        return .product(product.stableID)
    }

    func product(_ id: String) -> ProductDTO? { products[id] }
}

struct MainView: View {
    @Environment(AppState.self) private var app
    @State private var path: [MainRoute] = []
    @State private var store = ProductStore()

    var body: some View {
        NavigationStack(path: $path) {
            ChatView(path: $path)
                .navigationDestination(for: MainRoute.self) { route in
                    switch route {
                    case let .product(id):
                        if let product = store.product(id) {
                            ProductDetailView(product: product, path: $path)
                        }
                    case .favorites:
                        FavoritesView(path: $path)
                    case .profile:
                        ProfileView()
                    case .compare:
                        CompareView(products: store.compareProducts)
                    }
                }
        }
        .environment(store)
        .onChange(of: app.pendingProductLink, initial: true) { _, link in
            openPendingProduct(link)
        }
    }

    /// A tapped price-alert notification: open that favorite's product.
    private func openPendingProduct(_ link: String?) {
        guard let link else { return }
        app.pendingProductLink = nil
        Task {
            if app.favorites.isEmpty { _ = try? await app.client.refreshFavorites() }
            let product = app.favorites.first { $0.productLink == link }?.asProduct
                ?? ProductDTO(title: nil, price: nil, source: nil, link: link, thumbnail: nil)
            path.append(store.put(product))
        }
    }
}

struct ChatView: View {
    @Environment(AppState.self) private var app
    @Environment(ProductStore.self) private var store
    @Binding var path: [MainRoute]
    @State private var model: ChatViewModel?
    @State private var showDrawer = false
    @FocusState private var inputFocused: Bool

    var body: some View {
        Group {
            if let model {
                content(model)
            } else {
                Color.clear
            }
        }
        .synapScreen()
        .task {
            if model == nil { model = ChatViewModel(app: app) }
            await model?.onAppear()
        }
        .onChange(of: app.isPremium) { _, _ in
            Task {
                await model?.loadSessions()
                await model?.refreshQuota()
            }
        }
    }

    @ViewBuilder
    private func content(_ model: ChatViewModel) -> some View {
        @Bindable var model = model
        VStack(spacing: 0) {
            if let quota = model.quota {
                quotaBanner(left: quota.left, limit: quota.limit)
            }
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: Synap.spaceLg) {
                        if model.items.isEmpty && !model.isStreaming && !model.isLoadingMessages {
                            welcome(model)
                        }
                        ForEach(model.items) { item in
                            VStack(alignment: .leading, spacing: Synap.spaceMd) {
                                ChatBubble(text: item.text, isUser: item.isUser)
                                if !item.products.isEmpty {
                                    productRow(item.products)
                                }
                            }
                            .id(item.id)
                        }
                        if let status = model.agentStatus {
                            AgentStatusView(status: status)
                            if !model.pendingProducts.isEmpty { productRow(model.pendingProducts) }
                        }
                        Color.clear.frame(height: 1).id("bottom")
                    }
                    .padding(Synap.screenPadding)
                }
                .scrollDismissesKeyboard(.interactively)
                .onChange(of: model.items.count) { _, _ in withAnimation { proxy.scrollTo("bottom") } }
                .onChange(of: model.agentStatus) { _, _ in withAnimation { proxy.scrollTo("bottom") } }
                .overlay {
                    if model.isLoadingMessages { ProgressView().tint(Synap.primary) }
                }
            }
            inputBar(model)
        }
        .navigationTitle(model.currentTitle)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button { showDrawer = true } label: { Image(systemName: "line.3.horizontal") }
                    .accessibilityLabel(L("cd_open_menu"))
            }
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { model.newChat() } label: { Image(systemName: "square.and.pencil") }
                    .accessibilityLabel(L("chat_new"))
                Button { path.append(.favorites) } label: { Image(systemName: "heart") }
                    .accessibilityLabel(L("favorites_title"))
                Button { path.append(.profile) } label: { Image(systemName: "person.crop.circle") }
                    .accessibilityLabel(L("profile_title"))
            }
        }
        .sheet(isPresented: $showDrawer) {
            SessionDrawer(model: model) { session in
                showDrawer = false
                Task { await model.open(session) }
            } onNewChat: {
                showDrawer = false
                model.newChat()
            }
            .presentationDetents([.large])
        }
    }

    private func welcome(_ model: ChatViewModel) -> some View {
        VStack(alignment: .leading, spacing: Synap.spaceLg) {
            Text(app.userName.map { L("dashboard_hello_name", $0) } ?? L("dashboard_hello"))
                .font(.synapDisplay)
                .foregroundStyle(Synap.brandGradient)
            Text(L("dashboard_prompt")).font(.synapTitle)
            Text(L("dashboard_try")).font(.synapCaption).foregroundStyle(Synap.textMuted)
            ForEach(["suggestion_laptop", "suggestion_headphones", "suggestion_phone", "suggestion_watch"], id: \.self) { key in
                Button {
                    Task { await model.send(L(key)) }
                } label: {
                    HStack {
                        Text(L(key)).font(.synapCallout).multilineTextAlignment(.leading)
                        Spacer()
                        Image(systemName: "arrow.up.right").foregroundStyle(Synap.primary)
                    }
                    .synapCard(padding: Synap.spaceMd)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.top, Synap.spaceXl)
    }

    private func productRow(_ products: [ProductDTO]) -> some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: Synap.spaceMd) {
                    ForEach(products, id: \.stableID) { product in
                        ProductCard(
                            product: product,
                            currency: app.currency,
                            isFavorite: app.isFavorite(product),
                            hasAlert: app.favorite(for: product)?.targetPriceValue != nil,
                            onFavorite: { Task { await app.toggleFavorite(product) } }
                        )
                        .onTapGesture { path.append(store.put(product, siblings: products)) }
                    }
                }
            }
            if products.count >= 2 {
                Button {
                    openCompare(Array(products.prefix(4)))
                } label: {
                    Label(L("product_compare"), systemImage: "square.split.2x1").font(.synapCallout)
                }
            }
        }
    }

    private func openCompare(_ products: [ProductDTO]) {
        guard app.isPremium else {
            app.showPaywall = true
            return
        }
        store.compareProducts = products
        path.append(.compare)
    }

    private func quotaBanner(left: Int, limit: Int) -> some View {
        HStack {
            Text(LP("quota_left", count: left, left, limit)).font(.synapCaption)
            Spacer()
            Button(L("action_upgrade")) { app.showPaywall = true }
                .font(.synapCaption.weight(.bold))
                .foregroundStyle(Synap.premiumGold)
        }
        .padding(.horizontal, Synap.screenPadding)
        .padding(.vertical, Synap.spaceSm)
        .background(Synap.surfaceHigh)
    }

    private func inputBar(_ model: ChatViewModel) -> some View {
        @Bindable var model = model
        return HStack(alignment: .bottom, spacing: Synap.spaceSm) {
            TextField(L("chat_input_placeholder"), text: $model.input, axis: .vertical)
                .font(.synapBody)
                .lineLimit(1...5)
                .focused($inputFocused)
                .padding(.horizontal, Synap.spaceLg)
                .padding(.vertical, Synap.spaceMd)
                .background(Synap.surface)
                .overlay(RoundedRectangle(cornerRadius: Synap.radiusXl).stroke(Synap.outline, lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: Synap.radiusXl))
                .submitLabel(.send)
                .onSubmit { Task { await model.send() } }
            Button {
                Task { await model.send() }
            } label: {
                Image(systemName: "arrow.up")
                    .font(.headline)
                    .foregroundStyle(Synap.onPrimary)
                    .frame(width: 44, height: 44)
                    .background(Synap.brandGradient)
                    .clipShape(Circle())
            }
            .disabled(model.input.trimmingCharacters(in: .whitespaces).isEmpty || model.isStreaming)
            .opacity(model.input.trimmingCharacters(in: .whitespaces).isEmpty || model.isStreaming ? 0.5 : 1)
            .accessibilityLabel(L("cd_send"))
        }
        .padding(.horizontal, Synap.screenPadding)
        .padding(.vertical, Synap.spaceSm)
        .background(Synap.bg)
    }
}

/// Chat history: grouped by day, rename/delete via context menu, locked chats open the paywall.
struct SessionDrawer: View {
    let model: ChatViewModel
    let onSelect: (SessionResponse) -> Void
    let onNewChat: () -> Void
    @State private var renaming: SessionResponse?
    @State private var renameText = ""
    @State private var deleting: SessionResponse?

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button(action: onNewChat) {
                        Label(L("chat_new"), systemImage: "plus.bubble")
                    }
                }
                if model.sessions.isEmpty {
                    Text(L("chat_history_empty")).foregroundStyle(Synap.textMuted)
                }
                ForEach(model.groupedSessions, id: \.group) { group in
                    Section(group.group.title) {
                        ForEach(group.sessions) { session in
                            row(session)
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(Synap.bg)
            .navigationTitle(L("chat_history_title"))
            .navigationBarTitleDisplayMode(.inline)
            .alert(L("chat_rename_title"), isPresented: Binding(get: { renaming != nil }, set: { if !$0 { renaming = nil } })) {
                TextField(L("chat_rename_label"), text: $renameText)
                Button(L("action_cancel"), role: .cancel) { renaming = nil }
                Button(L("action_save")) {
                    if let session = renaming { Task { await model.rename(session, to: renameText) } }
                    renaming = nil
                }
            }
            .alert(L("chat_delete_title"), isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } })) {
                Button(L("action_cancel"), role: .cancel) { deleting = nil }
                Button(L("action_delete"), role: .destructive) {
                    if let session = deleting { Task { await model.delete(session) } }
                    deleting = nil
                }
            } message: {
                Text(L("chat_delete_message", deleting?.title ?? L("chat_untitled")))
            }
        }
        .preferredColorScheme(.dark)
    }

    private func row(_ session: SessionResponse) -> some View {
        Button { onSelect(session) } label: {
            HStack {
                Text(session.title ?? L("chat_untitled")).lineLimit(1)
                Spacer()
                if session.locked?.boolValue == true {
                    Image(systemName: "lock.fill").foregroundStyle(Synap.premiumGold)
                        .accessibilityLabel(L("chat_locked"))
                } else if session.id == model.currentSessionId {
                    Image(systemName: "checkmark").foregroundStyle(Synap.primary)
                }
            }
            .font(.synapCallout)
        }
        .foregroundStyle(Synap.text)
        .contextMenu {
            Button { renameText = session.title ?? ""; renaming = session } label: {
                Label(L("action_rename"), systemImage: "pencil")
            }
            Button(role: .destructive) { deleting = session } label: {
                Label(L("action_delete"), systemImage: "trash")
            }
        }
        .swipeActions {
            Button(role: .destructive) { deleting = session } label: { Label(L("action_delete"), systemImage: "trash") }
        }
    }
}
