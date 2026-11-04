import Foundation
import Observation
import Shared
import StoreKit

/// Premium through StoreKit 2 (auto-renewable subscription group `synapcart_premium`).
///
/// Every verified transaction is sent to the backend (`POST /billing/apple/verify`, which checks
/// Apple's signature chain again) and finished only after the server accepts it, so an unverified
/// purchase is retried on the next launch. `appAccountToken` is the user's id: the backend refuses
/// a transaction bound to another account.
@MainActor
@Observable
final class StoreKitManager {
    enum LoadState: Equatable { case idle, loading, loaded, unavailable }
    enum Outcome { case success, pending, cancelled, failed(String) }

    private(set) var products: [Product] = []
    private(set) var loadState: LoadState = .idle
    private(set) var isPurchasing = false

    weak var app: AppState?
    private let client: SynapCartClient
    private var updatesTask: Task<Void, Never>?

    init(client: SynapCartClient) {
        self.client = client
    }

    var monthly: Product? { products.first { $0.id == AppConfig.premiumMonthlyID } }
    var yearly: Product? { products.first { $0.id == AppConfig.premiumYearlyID } }

    /// Starts listening for renewals, Ask to Buy approvals and purchases made on other devices.
    func start() {
        guard updatesTask == nil else { return }
        updatesTask = Task { [weak self] in
            for await update in Transaction.updates {
                _ = await self?.process(update)
            }
        }
    }

    func loadProducts() async {
        guard loadState != .loading else { return }
        loadState = .loading
        do {
            let loaded = try await Product.products(for: AppConfig.premiumProductIDs)
            products = loaded.sorted { $0.price < $1.price }
            loadState = products.isEmpty ? .unavailable : .loaded
        } catch {
            loadState = .unavailable
        }
    }

    func purchase(_ product: Product) async -> Outcome {
        guard !isPurchasing else { return .cancelled }
        isPurchasing = true
        defer { isPurchasing = false }

        var options: Set<Product.PurchaseOption> = []
        if let userId = app?.plan?.userId ?? (try? await client.profile())?.id,
           let token = UUID(uuidString: userId) {
            options.insert(.appAccountToken(token))
        }
        do {
            switch try await product.purchase(options: options) {
            case let .success(verification):
                return await process(verification)
            case .pending:
                return .pending
            case .userCancelled:
                return .cancelled
            @unknown default:
                return .cancelled
            }
        } catch {
            return .failed(L("billing_error"))
        }
    }

    /// "Restore purchases": syncs with the App Store, then re-verifies current entitlements.
    func restore() async -> Bool {
        try? await AppStore.sync()
        return await syncEntitlements(silent: false)
    }

    /// Re-sends active subscriptions to the backend (new device, reinstall, missed verify).
    @discardableResult
    func syncEntitlements(silent: Bool) async -> Bool {
        guard (try? await client.isSignedIn().boolValue) == true else { return false }
        var found = false
        for await result in Transaction.currentEntitlements {
            guard case let .verified(transaction) = result,
                  AppConfig.premiumProductIDs.contains(transaction.productID) else { continue }
            found = true
            if case .failed = await process(result), !silent {
                app?.show(L("billing_verify_failed"), isError: true)
            }
        }
        return found
    }

    private func process(_ result: VerificationResult<Transaction>) async -> Outcome {
        // StoreKit's local check first; the backend verifies the same JWS independently.
        guard case let .verified(transaction) = result else { return .failed(L("billing_verify_failed")) }
        guard AppConfig.premiumProductIDs.contains(transaction.productID) else {
            await transaction.finish()
            return .cancelled
        }
        guard (try? await client.isSignedIn().boolValue) == true else {
            // Not signed in: leave it unfinished; it is verified after login.
            return .pending
        }
        do {
            _ = try await client.verifyApplePurchase(signedTransaction: result.jwsRepresentation)
            await transaction.finish()
            return .success
        } catch {
            return .failed(error.userMessage(fallback: "billing_verify_failed"))
        }
    }

    // MARK: - Display helpers

    func periodLabel(_ product: Product) -> String {
        product.id == AppConfig.premiumYearlyID
            ? L("paywall_price_year", product.displayPrice)
            : L("paywall_price_month", product.displayPrice)
    }

    /// Percent saved by the yearly plan versus 12 monthly payments.
    var yearlySavingPercent: Int? {
        guard let monthly, let yearly else { return nil }
        let twelve = monthly.price * 12
        guard twelve > 0 else { return nil }
        let saving = (twelve - yearly.price) / twelve * 100
        let value = NSDecimalNumber(decimal: saving).intValue
        return value > 0 ? value : nil
    }

    func hasFreeTrial(_ product: Product) -> Bool {
        product.subscription?.introductoryOffer?.paymentMode == .freeTrial
    }
}
