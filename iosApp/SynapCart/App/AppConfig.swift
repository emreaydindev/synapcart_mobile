import Foundation

/// Build-time configuration from `Configs/*.xcconfig`, surfaced through Info.plist.
enum AppConfig {
    private static func info(_ key: String) -> String? {
        (Bundle.main.object(forInfoDictionaryKey: key) as? String)?.trimmingCharacters(in: .whitespaces)
    }

    /// Backend origin with a trailing slash (`https://api.synapcart.emreaydin.dev/` in Release).
    static let apiBaseURL: String = info("SynapAPIBaseURL").flatMap { $0.isEmpty ? nil : $0 }
        ?? "https://api.synapcart.emreaydin.dev/"

    /// Web portal: privacy, terms, support and account-deletion pages.
    static let webURL: URL = URL(string: info("SynapWebURL") ?? "") ?? URL(string: "https://synapcart.emreaydin.dev/")!

    static var privacyURL: URL { webURL.appendingPathComponent("privacy") }
    static var termsURL: URL { webURL.appendingPathComponent("terms") }
    static var supportURL: URL { webURL.appendingPathComponent("support") }
    /// Apple's standard EULA, required on the paywall for auto-renewable subscriptions.
    static let appleEULA = URL(string: "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/")!

    /// Auto-renewable subscription products (App Store Connect), monthly first.
    static let premiumProductIDs: [String] = (info("SynapPremiumProductIDs") ?? "synapcart_premium_monthly,synapcart_premium_yearly")
        .split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
    static let premiumMonthlyID = info("SynapPremiumMonthly") ?? "synapcart_premium_monthly"
    static let premiumYearlyID = info("SynapPremiumYearly") ?? "synapcart_premium_yearly"

    static var appVersion: String {
        (Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String) ?? "1.0"
    }

    #if DEBUG
    static let isDebug = true
    #else
    static let isDebug = false
    #endif
}
