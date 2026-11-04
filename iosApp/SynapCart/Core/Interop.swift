import Foundation
import Shared

// Helpers for the Kotlin `Shared` framework (shared/ KMP module). Kotlin nullable numbers
// arrive boxed (KotlinDouble?, KotlinInt?), errors arrive as NSError carrying the Kotlin
// exception, and Kotlin classes are reference types without Swift protocol conformances.

extension Error {
    /// The shared module's typed failure, when the error came from a Kotlin call.
    var apiException: ApiException? { (self as NSError).kotlinException as? ApiException }

    /// 402 plan limit (quota_exceeded / premium_required / history_locked): open the paywall.
    var isPlanLimit: Bool { apiException is ApiException.QuotaExceeded }

    /// User-presentable text: the backend's `detail` (already localized) or a fallback.
    func userMessage(fallback: String = "error_generic") -> String {
        guard let api = apiException else { return L(fallback) }
        return api.userMessage(fallback: fallback)
    }
}

extension ApiException {
    func userMessage(fallback: String = "error_generic") -> String {
        if let detail, !detail.isEmpty { return detail }
        switch self {
        case is ApiException.Network: return L("error_network")
        case is ApiException.Unauthorized: return L("error_unauthorized")
        case is ApiException.QuotaExceeded: return L("error_quota")
        case is ApiException.RateLimited: return L("error_rate_limited")
        case let validation as ApiException.Validation: return L(Self.validationKey(validation.reason))
        default: return L(fallback)
        }
    }

    /// Maps shared `ValidationReasons` to string keys (same mapping as Android's ApiErrors.kt).
    static func validationKey(_ reason: String) -> String {
        switch reason {
        case "email_empty": return "error_email_empty"
        case "email_password_empty": return "error_email_password_empty"
        case "name_empty": return "error_name_empty"
        case "password_fields_empty": return "error_password_fields_empty"
        case "password_too_short": return "password_too_short"
        case "password_too_long": return "password_too_long"
        case "password_needs_letters_digits": return "password_needs_letters_digits"
        case "product_no_link": return "error_product_no_link"
        default: return "error_generic"
        }
    }
}

extension KotlinDouble {
    static func from(_ value: Double?) -> KotlinDouble? { value.map { KotlinDouble(value: $0) } }
}

extension ProductDTO {
    var priceValue: Double? { price?.doubleValue }
    /// Stable identity for lists (products have no id; the link is unique per offer).
    var stableID: String { link ?? title ?? "product" }
}

extension FavoriteResponse {
    var asProduct: ProductDTO {
        ProductDTO(title: productTitle, price: price, source: source, link: productLink, thumbnail: thumbnailUrl)
    }
    var priceValue: Double? { price?.doubleValue }
    var targetPriceValue: Double? { targetPrice?.doubleValue }
}

extension SessionResponse: Identifiable {}
extension FavoriteResponse: Identifiable {}

/// Password rules shared with Android and the backend (shared `PasswordPolicy`).
enum PasswordRules {
    static func problem(_ password: String) -> String? {
        PasswordPolicy.shared.check(password: password).map { L(ApiException.validationKey($0)) }
    }
}

/// Wraps a shared [WatchHandle] so SwiftUI owners cancel it on deinit.
final class ObservationBag {
    private var handles: [WatchHandle] = []
    func add(_ handle: WatchHandle) { handles.append(handle) }
    func cancelAll() { handles.forEach { $0.cancel() }; handles.removeAll() }
    deinit { cancelAll() }
}
