import Shared
import SwiftUI

/// Side-by-side products plus the AI pros/cons and verdict (`POST /chat/compare`, premium).
struct CompareView: View {
    @Environment(AppState.self) private var app
    let products: [ProductDTO]
    @State private var result: CompareResponse?
    @State private var isLoading = false
    @State private var error: String?

    private var cheapestIndex: Int? {
        products.indices.filter { products[$0].priceValue != nil }.min { products[$0].priceValue! < products[$1].priceValue! }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Synap.spaceLg) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(alignment: .top, spacing: Synap.spaceMd) {
                        ForEach(Array(products.enumerated()), id: \.offset) { index, product in
                            column(product, index: index)
                        }
                    }
                }

                Text(L("compare_ai_title")).font(.synapTitle)
                if isLoading {
                    HStack(spacing: Synap.spaceSm) {
                        ProgressView().tint(Synap.primary)
                        Text(L("compare_loading")).foregroundStyle(Synap.textMuted)
                    }
                } else if let error {
                    ErrorBanner(message: error)
                    Button(L("action_retry")) { Task { await load() } }.buttonStyle(SecondaryButtonStyle())
                } else if let result {
                    if let verdict = result.verdict, !verdict.isEmpty {
                        MarkdownText(text: verdict).synapCard()
                    }
                    ForEach(Array((result.items ?? []).enumerated()), id: \.offset) { index, item in
                        analysis(item, isBest: result.bestIndex?.intValue == index)
                    }
                }
            }
            .padding(Synap.screenPadding)
        }
        .synapScreen()
        .navigationTitle(L("compare_title"))
        .navigationBarTitleDisplayMode(.inline)
        .task { if result == nil { await load() } }
    }

    private func column(_ product: ProductDTO, index: Int) -> some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            ProductThumbnail(url: product.thumbnail, size: 120).frame(maxWidth: .infinity)
            Text(product.title ?? L("product_unknown")).font(.synapCallout).lineLimit(3)
            Text(L("compare_price")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            Text(Formatters.price(product.priceValue, currency: app.currency) ?? L("product_price_unknown"))
                .font(.synapHeadline).foregroundStyle(Synap.primary)
            if cheapestIndex == index {
                Text(L("compare_cheapest")).font(.synapCaption).foregroundStyle(Synap.onPrimary)
                    .padding(.horizontal, 8).padding(.vertical, 2).background(Synap.primary).clipShape(Capsule())
            }
            Text(L("compare_store")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            Text(product.source ?? L("product_unknown_store")).font(.synapCallout)
        }
        .frame(width: 170)
        .synapCard(padding: Synap.spaceMd)
    }

    private func analysis(_ item: CompareItem, isBest: Bool) -> some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            HStack {
                Text(item.title ?? L("product_unknown")).font(.synapHeadline)
                Spacer()
                if isBest {
                    Label(L("compare_best"), systemImage: "star.fill").font(.synapCaption).foregroundStyle(Synap.premiumGold)
                }
            }
            if let pros = item.pros, !pros.isEmpty {
                Text(L("compare_pros")).font(.synapCaption).foregroundStyle(Synap.primary)
                ForEach(pros, id: \.self) { Label($0, systemImage: "plus.circle.fill").font(.synapCallout) }
            }
            if let cons = item.cons, !cons.isEmpty {
                Text(L("compare_cons")).font(.synapCaption).foregroundStyle(Synap.error)
                ForEach(cons, id: \.self) { Label($0, systemImage: "minus.circle.fill").font(.synapCallout) }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .synapCard()
        .overlay(RoundedRectangle(cornerRadius: Synap.radiusLg).stroke(isBest ? Synap.premiumGold : .clear, lineWidth: 1.5))
    }

    private func load() async {
        isLoading = true
        error = nil
        defer { isLoading = false }
        do {
            result = try await app.client.compare(products: products)
        } catch {
            if error.isPlanLimit { app.showPaywall = true }
            self.error = error.userMessage(fallback: "error_compare")
        }
    }
}
