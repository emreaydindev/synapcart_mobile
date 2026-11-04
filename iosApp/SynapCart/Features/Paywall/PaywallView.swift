import StoreKit
import SwiftUI

/// Premium paywall: plan comparison (matches the backend's plan limits and the web pricing),
/// StoreKit products with prices from the App Store, purchase/restore, and the terms Apple
/// requires for auto-renewable subscriptions (price, period, renewal, EULA and privacy links).
struct PaywallView: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    @State private var selectedID = AppConfig.premiumYearlyID
    @State private var web: WebLink?
    @State private var showManage = false
    @State private var isRestoring = false

    private var store: StoreKitManager { app.store }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: Synap.spaceXl) {
                    header
                    if app.isPremium {
                        activeCard
                    } else {
                        comparisonTable
                        plans
                        purchaseButton
                    }
                    footer
                }
                .padding(Synap.screenPadding)
            }
            .synapScreen()
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dismiss() } label: { Image(systemName: "xmark") }
                        .accessibilityLabel(L("action_close"))
                }
            }
        }
        .task { if store.products.isEmpty { await store.loadProducts() } }
        .sheet(item: $web) { SafariView(url: $0.url).ignoresSafeArea() }
        .manageSubscriptionsSheet(isPresented: $showManage)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            PremiumBadge()
            Text(L("paywall_title")).font(.synapDisplay)
            Text(L("paywall_subtitle")).font(.synapBody).foregroundStyle(Synap.textMuted)
        }
    }

    private var activeCard: some View {
        VStack(alignment: .leading, spacing: Synap.spaceMd) {
            if let expiry = Formatters.timestamp(app.plan?.expiresAt) {
                Text(L("paywall_premium_until", Formatters.mediumDate(expiry))).font(.synapHeadline)
            } else {
                Text(L("paywall_premium_active")).font(.synapHeadline)
            }
            PremiumFeatureList()
            if app.planSource == "google" {
                Text(L("paywall_managed_google")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            } else {
                Button(L("plan_manage")) { showManage = true }.buttonStyle(SecondaryButtonStyle())
            }
        }
        .synapCard()
    }

    private var comparisonTable: some View {
        VStack(spacing: 0) {
            tableRow("", free: L("plan_free"), premium: L("plan_premium"), header: true)
            tableRow(L("paywall_row_messages"), free: "5", premium: "100")
            tableRow(L("paywall_row_products"), free: "3", premium: "8")
            tableRow(L("paywall_row_deep"), free: "—", premium: "✓")
            tableRow(L("paywall_row_alerts"), free: "—", premium: "✓")
            tableRow(L("paywall_row_compare"), free: "—", premium: "✓")
            tableRow(L("paywall_row_history"), free: L("paywall_history_free"), premium: L("paywall_unlimited"))
        }
        .synapCard(padding: 0)
    }

    private func tableRow(_ title: String, free: String, premium: String, header: Bool = false) -> some View {
        HStack {
            Text(title).frame(maxWidth: .infinity, alignment: .leading)
            Text(free).frame(width: 72).foregroundStyle(Synap.textMuted)
            Text(premium).frame(width: 84).foregroundStyle(header ? Synap.premiumGold : Synap.primary)
        }
        .font(header ? .synapCaption : .synapCallout)
        .padding(.horizontal, Synap.spaceLg)
        .padding(.vertical, Synap.spaceMd)
        .overlay(alignment: .bottom) { Divider().overlay(Synap.outline) }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var plans: some View {
        switch store.loadState {
        case .idle, .loading:
            HStack(spacing: Synap.spaceSm) {
                ProgressView().tint(Synap.primary)
                Text(L("paywall_loading")).foregroundStyle(Synap.textMuted)
            }
        case .unavailable:
            ErrorBanner(message: L("paywall_unavailable_ios"))
            Button(L("action_retry")) { Task { await store.loadProducts() } }.buttonStyle(SecondaryButtonStyle())
        case .loaded:
            VStack(spacing: Synap.spaceMd) {
                ForEach(store.products, id: \.id) { product in
                    planOption(product)
                }
            }
        }
    }

    private func planOption(_ product: Product) -> some View {
        let selected = product.id == selectedID
        let isYearly = product.id == AppConfig.premiumYearlyID
        return Button { selectedID = product.id } label: {
            HStack {
                Image(systemName: selected ? "largecircle.fill.circle" : "circle")
                    .foregroundStyle(selected ? Synap.premiumGold : Synap.textMuted)
                VStack(alignment: .leading, spacing: 2) {
                    Text(L(isYearly ? "paywall_yearly" : "paywall_monthly")).font(.synapHeadline)
                    Text(store.periodLabel(product)).font(.synapCallout).foregroundStyle(Synap.textMuted)
                }
                Spacer()
                if isYearly, let saving = store.yearlySavingPercent {
                    VStack(alignment: .trailing, spacing: 2) {
                        Text(L("paywall_best_value")).font(.synapCaption).foregroundStyle(Synap.premiumGold)
                        Text(L("paywall_save", saving)).font(.synapCaption)
                    }
                }
            }
            .padding(Synap.spaceLg)
            .background(Synap.surface)
            .overlay(
                RoundedRectangle(cornerRadius: Synap.radiusLg)
                    .stroke(selected ? Synap.premiumGold : Synap.outline, lineWidth: selected ? 2 : 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg))
        }
        .buttonStyle(.plain)
        .foregroundStyle(Synap.text)
    }

    private var selectedProduct: Product? {
        store.products.first { $0.id == selectedID } ?? store.products.first
    }

    private var purchaseButton: some View {
        Button(selectedProduct.map { store.hasFreeTrial($0) } == true ? L("paywall_free_trial") : L("paywall_subscribe")) {
            guard let product = selectedProduct else { return }
            Task {
                switch await store.purchase(product) {
                case .success:
                    app.show(L("billing_success"))
                    dismiss()
                case .pending:
                    app.show(L("billing_pending"))
                case .cancelled:
                    break
                case let .failed(message):
                    app.show(message, isError: true)
                }
            }
        }
        .buttonStyle(PremiumButtonStyle(isLoading: store.isPurchasing))
        .disabled(selectedProduct == nil || store.isPurchasing)
    }

    private var footer: some View {
        VStack(alignment: .leading, spacing: Synap.spaceMd) {
            if !app.isPremium {
                Button {
                    isRestoring = true
                    Task {
                        defer { isRestoring = false }
                        let restored = await store.restore()
                        app.show(L(restored ? "billing_restored" : "billing_nothing_to_restore"), isError: !restored)
                        if restored && app.isPremium { dismiss() }
                    }
                } label: {
                    if isRestoring { ProgressView() } else { Text(L("paywall_restore")) }
                }
                .font(.synapCallout)
                .frame(maxWidth: .infinity)
            }
            Text(L("paywall_legal_ios")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            HStack(spacing: Synap.spaceLg) {
                Button(L("paywall_eula")) { web = WebLink(url: AppConfig.appleEULA) }
                Button(L("legal_terms")) { web = WebLink(url: AppConfig.termsURL) }
                Button(L("legal_privacy")) { web = WebLink(url: AppConfig.privacyURL) }
            }
            .font(.synapCaption)
        }
    }
}
