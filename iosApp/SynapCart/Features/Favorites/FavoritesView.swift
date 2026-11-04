import Shared
import SwiftUI

struct FavoritesView: View {
    @Environment(AppState.self) private var app
    @Environment(ProductStore.self) private var store
    @Binding var path: [MainRoute]
    @State private var isLoading = false
    @State private var selecting = false
    @State private var selection: Set<Int32> = []

    private let maxCompare = 4

    var body: some View {
        Group {
            if app.favorites.isEmpty && !isLoading {
                EmptyStateView(systemImage: "heart", title: L("favorites_empty_title"), message: L("favorites_empty_message"))
            } else {
                List {
                    ForEach(app.favorites) { favorite in
                        row(favorite)
                            .listRowBackground(Synap.surface)
                    }
                    .onDelete(perform: delete)
                }
                .scrollContentBackground(.hidden)
                .refreshable { await refresh() }
            }
        }
        .synapScreen()
        .navigationTitle(selecting ? LP("compare_selected", count: selection.count, selection.count) : L("favorites_title"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                if app.favorites.count >= 2 {
                    Button(selecting ? L("action_cancel") : L("favorites_select")) {
                        selecting.toggle()
                        selection = []
                    }
                }
            }
            if selecting {
                ToolbarItem(placement: .bottomBar) {
                    Button(L("compare_action", selection.count)) { compare() }
                        .disabled(selection.count < 2)
                }
            }
        }
        .overlay { if isLoading && app.favorites.isEmpty { ProgressView().tint(Synap.primary) } }
        .task { await refresh() }
    }

    private func row(_ favorite: FavoriteResponse) -> some View {
        Button {
            if selecting {
                toggleSelection(favorite)
            } else {
                path.append(store.put(favorite.asProduct, siblings: app.favorites.map(\.asProduct)))
            }
        } label: {
            HStack(spacing: Synap.spaceMd) {
                if selecting {
                    Image(systemName: selection.contains(favorite.id) ? "checkmark.circle.fill" : "circle")
                        .foregroundStyle(Synap.primary)
                }
                ProductThumbnail(url: favorite.thumbnailUrl, size: 64)
                VStack(alignment: .leading, spacing: 4) {
                    Text(favorite.productTitle ?? L("product_unknown")).font(.synapCallout).lineLimit(2)
                    HStack(spacing: Synap.spaceXs) {
                        Text(Formatters.price(favorite.priceValue, currency: favorite.currency ?? app.currency) ?? L("product_price_unknown"))
                            .font(.synapHeadline)
                            .foregroundStyle(Synap.primary)
                        if favorite.targetPriceValue != nil {
                            Image(systemName: "bell.fill").font(.caption).foregroundStyle(Synap.premiumGold)
                                .accessibilityLabel(L("cd_price_alert_on"))
                        }
                    }
                    Text(favorite.source ?? L("product_unknown_store")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
                }
            }
        }
        .foregroundStyle(Synap.text)
    }

    private func toggleSelection(_ favorite: FavoriteResponse) {
        if selection.contains(favorite.id) {
            selection.remove(favorite.id)
        } else if selection.count < maxCompare {
            selection.insert(favorite.id)
        } else {
            app.show(LP("compare_max", count: maxCompare, maxCompare), isError: true)
        }
    }

    private func compare() {
        guard app.isPremium else {
            app.showPaywall = true
            return
        }
        store.compareProducts = app.favorites.filter { selection.contains($0.id) }.map(\.asProduct)
        selecting = false
        selection = []
        path.append(.compare)
    }

    private func refresh() async {
        isLoading = true
        defer { isLoading = false }
        do {
            _ = try await app.client.refreshFavorites()
        } catch {
            app.handle(error, fallback: "error_favorites_load")
        }
    }

    private func delete(at offsets: IndexSet) {
        let targets = offsets.map { app.favorites[$0] }
        Task {
            for favorite in targets {
                do {
                    _ = try await app.client.removeFavorite(favorite: favorite)
                } catch {
                    app.handle(error, fallback: "error_favorite_update")
                }
            }
        }
    }
}
