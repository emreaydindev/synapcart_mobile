import Shared
import SwiftUI

struct ProductDetailView: View {
    @Environment(AppState.self) private var app
    @Environment(ProductStore.self) private var store
    let product: ProductDTO
    @Binding var path: [MainRoute]
    @State private var web: WebLink?
    @State private var showAlertSheet = false

    private var favorite: FavoriteResponse? { app.favorite(for: product) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Synap.spaceLg) {
                ProductThumbnail(url: product.thumbnail, size: 260)
                    .frame(maxWidth: .infinity)
                Text(product.title ?? L("product_unknown")).font(.synapTitle)
                HStack(alignment: .firstTextBaseline) {
                    Text(Formatters.price(product.priceValue, currency: app.currency) ?? L("product_price_unknown"))
                        .font(.jakarta(26, .extraBold))
                        .foregroundStyle(Synap.primary)
                    Spacer()
                    Text(product.source ?? L("product_unknown_store"))
                        .font(.synapCallout)
                        .foregroundStyle(Synap.textMuted)
                }
                if let target = favorite?.targetPriceValue,
                   let label = Formatters.price(target, currency: favorite?.currency ?? app.currency) {
                    Label(L("product_price_alert_on", label), systemImage: "bell.fill")
                        .font(.synapCallout)
                        .foregroundStyle(Synap.premiumGold)
                }

                if let link = product.link, let url = URL(string: link) {
                    Button(L("product_go_to_store", product.source ?? L("product_store_web"))) {
                        web = WebLink(url: url)
                    }
                    .buttonStyle(PrimaryButtonStyle())
                }

                HStack(spacing: Synap.spaceMd) {
                    Button {
                        Task { await app.toggleFavorite(product) }
                    } label: {
                        Label(L(app.isFavorite(product) ? "cd_remove_favorite" : "cd_add_favorite"),
                              systemImage: app.isFavorite(product) ? "heart.fill" : "heart")
                    }
                    .buttonStyle(SecondaryButtonStyle(tint: app.isFavorite(product) ? Synap.error : Synap.text))

                    Button {
                        if app.isPremium { showAlertSheet = true } else { app.showPaywall = true }
                    } label: {
                        Label(L("product_price_alert"), systemImage: "bell")
                    }
                    .buttonStyle(SecondaryButtonStyle(tint: Synap.premiumGold))
                }

                if store.siblings.count >= 2 {
                    Button {
                        if app.isPremium {
                            store.compareProducts = Array(store.siblings.prefix(4))
                            path.append(.compare)
                        } else {
                            app.showPaywall = true
                        }
                    } label: {
                        Label(L("product_compare"), systemImage: "square.split.2x1")
                    }
                    .buttonStyle(SecondaryButtonStyle())
                }

                Text(L("product_found_note"))
                    .font(.synapFootnote)
                    .foregroundStyle(Synap.textMuted)
            }
            .padding(Synap.screenPadding)
        }
        .synapScreen()
        .navigationTitle(L("product_details_title"))
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $web) { SafariView(url: $0.url).ignoresSafeArea() }
        .sheet(isPresented: $showAlertSheet) {
            PriceAlertSheet(product: product, existingTarget: favorite?.targetPriceValue)
                .presentationDetents([.medium])
        }
    }
}

/// Sets or removes a price alert (premium). Favorites the product first if needed.
struct PriceAlertSheet: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    let product: ProductDTO
    let existingTarget: Double?
    @State private var text = ""
    @State private var isSaving = false
    @State private var error: String?

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: Synap.spaceLg) {
                Text(L("price_alert_message")).font(.synapCallout).foregroundStyle(Synap.textMuted)
                SynapTextField(title: L("price_alert_target_label", app.currency), text: $text,
                               keyboard: .decimalPad, error: error)
                Button(L("price_alert_set")) { save(Formatters.parsePrice(text), requireValue: true) }
                    .buttonStyle(PrimaryButtonStyle(isLoading: isSaving))
                if existingTarget != nil {
                    Button(L("price_alert_remove")) { save(nil, requireValue: false) }
                        .buttonStyle(SecondaryButtonStyle(tint: Synap.error))
                }
                Spacer()
            }
            .padding(Synap.screenPadding)
            .synapScreen()
            .navigationTitle(L("price_alert_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("action_cancel")) { dismiss() } }
            }
        }
        .onAppear {
            let initial = existingTarget ?? Formatters.suggestedTarget(product.priceValue)
            if let initial { text = String(format: "%.0f", initial) }
        }
    }

    private func save(_ target: Double?, requireValue: Bool) {
        if requireValue && target == nil {
            error = L("price_alert_invalid")
            return
        }
        isSaving = true
        Task {
            defer { isSaving = false }
            do {
                _ = try await app.client.setPriceAlert(product: product, targetPrice: KotlinDouble.from(target))
                if let target, let label = Formatters.price(target, currency: app.currency) {
                    app.show(L("price_alert_saved", label))
                    await app.push.requestAuthorization()
                } else {
                    app.show(L("price_alert_removed"))
                }
                dismiss()
            } catch {
                app.handle(error, fallback: "error_price_alert")
            }
        }
    }
}
