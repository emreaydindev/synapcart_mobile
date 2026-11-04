import Foundation
import Observation
import Shared
import SwiftUI

struct Toast: Equatable, Identifiable {
    let id = UUID()
    let message: String
    var isError = false
}

/// App-wide state and the composition root: the shared Kotlin client, StoreKit and push.
@MainActor
@Observable
final class AppState {
    enum Route { case launching, onboarding, auth, main }

    private(set) var route: Route = .launching
    var toast: Toast?
    var showPaywall = false
    /// Token from a password-reset link (synapcart:// or a Universal Link) to show the reset form.
    var resetToken: String?
    /// Product link from a tapped price-alert notification; MainView opens it once signed in.
    var pendingProductLink: String?

    private(set) var plan: PlanState?
    var isPremium: Bool { plan?.isPremium ?? false }
    /// "apple" | "google" while premium.
    var planSource: String? { plan?.source }
    private(set) var favoriteLinks: Set<String> = []
    private(set) var favorites: [FavoriteResponse] = []
    private(set) var currency = "TRY"
    private(set) var userName: String?

    let client: SynapCartClient
    let store: StoreKitManager
    let push: PushManager
    private let hooks: Hooks
    private let observation = ObservationBag()

    init() {
        let hooks = Hooks()
        self.hooks = hooks
        client = IosPlatformKt.createSynapCartClient(baseUrl: AppConfig.apiBaseURL, hooks: hooks, logRequests: AppConfig.isDebug)
        store = StoreKitManager(client: client)
        push = PushManager(client: client)
        hooks.push = push
        store.app = self
        push.app = self

        observation.add(client.watchSessionExpired { [weak self] in
            guard let self, self.route == .main else { return }
            self.route = .auth
            self.show(L("error_unauthorized"), isError: true)
        })
        observation.add(client.watchPlan { [weak self] state in self?.plan = state })
        observation.add(client.watchFavorites { [weak self] list in self?.favorites = list })
        observation.add(client.watchFavoriteLinks { [weak self] links in self?.favoriteLinks = links })
    }

    // MARK: - Lifecycle

    func bootstrap() async {
        store.start()
        do {
            if !(try await client.isOnboardingCompleted().boolValue) {
                route = .onboarding
            } else if try await client.isSignedIn().boolValue {
                await enterMain()
            } else {
                route = .auth
            }
        } catch {
            route = .auth
        }
    }

    func finishOnboarding(language: String, currency: String) async {
        _ = try? await client.savePreferences(language: language, currency: currency)
        _ = try? await client.setOnboardingCompleted(done: true)
        route = (try? await client.isSignedIn().boolValue) == true ? .main : .auth
    }

    /// After login/registration (or at launch with a stored token).
    func enterMain() async {
        route = .main
        await refreshProfile()
        _ = try? await client.refreshFavorites()
        await store.syncEntitlements(silent: true)
        await push.syncTokenIfAuthorized()
    }

    func refreshProfile() async {
        if let profile = try? await client.profile() {
            userName = profile.fullName
            currency = profile.currency ?? currency
        } else {
            userName = try? await client.userName()
            currency = (try? await client.currency()) ?? currency
        }
    }

    func logout() async {
        _ = try? await client.logout()
        favorites = []
        favoriteLinks = []
        route = .auth
    }

    func accountDeleted() {
        favorites = []
        favoriteLinks = []
        route = .onboarding
        show(L("account_deleted"))
    }

    // MARK: - Favorites (shared cache)

    func isFavorite(_ product: ProductDTO) -> Bool {
        guard let link = product.link else { return false }
        return favoriteLinks.contains(link)
    }

    func favorite(for product: ProductDTO) -> FavoriteResponse? {
        favorites.first { $0.productLink == product.link }
    }

    func toggleFavorite(_ product: ProductDTO) async {
        do {
            let nowFavorite = try await client.toggleFavorite(product: product).boolValue
            show(L(nowFavorite ? "favorite_added" : "favorite_removed"))
            if nowFavorite { await push.requestAuthorizationOnce() }
        } catch {
            handle(error, fallback: "error_favorite_update")
        }
    }

    // MARK: - Feedback

    func show(_ message: String, isError: Bool = false) {
        withAnimation { toast = Toast(message: message, isError: isError) }
        let current = toast?.id
        Task {
            try? await Task.sleep(for: .seconds(3))
            if toast?.id == current { withAnimation { toast = nil } }
        }
    }

    /// Shows an error, opening the paywall for plan limits (unless already premium).
    func handle(_ error: Error, fallback: String = "error_generic") {
        if error.isPlanLimit && !isPremium {
            showPaywall = true
        }
        show(error.userMessage(fallback: fallback), isError: true)
    }

    // MARK: - Deep links

    func open(url: URL) {
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        let token = components?.queryItems?.first { $0.name == "token" }?.value
        let isReset = url.host == "reset-password" || url.path.hasPrefix("/reset-password")
        if isReset, let token, !token.isEmpty {
            resetToken = token
        }
        // synapcart://login just brings the app forward.
    }
}

/// Kotlin -> Swift callbacks: unregister the push token before logout.
final class Hooks: NSObject, IosHooks {
    weak var push: PushManager?

    func beforeSignOut(completion: @escaping () -> Void) {
        Task { @MainActor in
            await push?.unregister()
            completion()
        }
    }

    func onProfileLoaded(language: String?) {
        // The UI language follows iOS (per-app language in Settings); nothing to do.
    }
}
