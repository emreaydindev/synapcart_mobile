import FirebaseCore
import FirebaseMessaging
import Shared
import UIKit
import UserNotifications

/// Price-alert push notifications through Firebase Cloud Messaging (APNs key uploaded to Firebase).
/// Without `GoogleService-Info.plist` in the bundle Firebase isn't configured and this is a no-op,
/// like Android without google-services.json.
@MainActor
final class PushManager: NSObject {
    private let client: SynapCartClient
    weak var app: AppState?
    private var currentToken: String?

    nonisolated(unsafe) private static var firebaseConfigured = false

    nonisolated static func configureFirebaseIfAvailable() {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return }
        FirebaseApp.configure()
        firebaseConfigured = true
    }

    var isAvailable: Bool { Self.firebaseConfigured }

    init(client: SynapCartClient) {
        self.client = client
        super.init()
        UNUserNotificationCenter.current().delegate = self
        if Self.firebaseConfigured { Messaging.messaging().delegate = self }
    }

    /// Asks once (after the first favorite), like Android; later prompts come from price alerts.
    func requestAuthorizationOnce() async {
        guard isAvailable else { return }
        if (try? await client.notificationPromptShown().boolValue) == true { return }
        _ = try? await client.setNotificationPromptShown()
        await requestAuthorization()
    }

    /// Asks (if undecided) and registers for remote notifications.
    func requestAuthorization() async {
        guard isAvailable else { return }
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        if settings.authorizationStatus == .notDetermined {
            _ = try? await center.requestAuthorization(options: [.alert, .sound, .badge])
        }
        if await center.notificationSettings().authorizationStatus == .authorized {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }

    /// At sign-in: re-register if the user already allowed notifications.
    func syncTokenIfAuthorized() async {
        guard isAvailable else { return }
        if await UNUserNotificationCenter.current().notificationSettings().authorizationStatus == .authorized {
            UIApplication.shared.registerForRemoteNotifications()
            await sendToken()
        }
    }

    func didRegister(apnsToken: Data) {
        guard isAvailable else { return }
        Messaging.messaging().apnsToken = apnsToken
        Task { await sendToken() }
    }

    private func sendToken(_ token: String? = nil) async {
        guard isAvailable else { return }
        let value: String
        if let token {
            value = token
        } else if let fetched = try? await Messaging.messaging().token() {
            value = fetched
        } else {
            return
        }
        currentToken = value
        _ = try? await client.registerDeviceToken(token: value)
    }

    /// Before logout, so this device stops receiving the user's alerts.
    func unregister() async {
        guard isAvailable, let token = currentToken ?? (try? await Messaging.messaging().token()) else { return }
        _ = try? await client.unregisterDeviceToken(token: token)
    }
}

extension PushManager: MessagingDelegate {
    nonisolated func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken else { return }
        Task { @MainActor in await self.sendToken(fcmToken) }
    }
}

extension PushManager: UNUserNotificationCenterDelegate {
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound, .list]
    }

    /// Tapping a price alert opens that favorite's product (data: type, favorite_id, link, price).
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let info = response.notification.request.content.userInfo
        guard let link = info["link"] as? String else { return }
        await MainActor.run { self.app?.pendingProductLink = link }
    }
}
