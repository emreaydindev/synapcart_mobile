import SafariServices
import Shared
import SwiftUI

// MARK: - Brand

struct LogoLockup: View {
    var size: CGFloat = 36

    var body: some View {
        HStack(spacing: Synap.spaceSm) {
            Image("LogoMark").resizable().scaledToFit().frame(width: size, height: size)
            Text("SynapCart")
                .font(.jakarta(size * 0.62, .extraBold, relativeTo: .title))
                .foregroundStyle(Synap.brandGradient)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(L("cd_logo"))
    }
}

// MARK: - Inputs

struct SynapTextField: View {
    let title: String
    @Binding var text: String
    var isSecure = false
    var contentType: UITextContentType?
    var keyboard: UIKeyboardType = .default
    var autocapitalization: TextInputAutocapitalization = .sentences
    var error: String?

    @State private var revealed = false

    var body: some View {
        VStack(alignment: .leading, spacing: Synap.spaceXs) {
            Text(title).font(.synapCaption).foregroundStyle(Synap.textMuted)
            HStack {
                Group {
                    if isSecure && !revealed {
                        SecureField("", text: $text)
                    } else {
                        TextField("", text: $text)
                    }
                }
                .font(.synapBody)
                .textContentType(contentType)
                .keyboardType(keyboard)
                .textInputAutocapitalization(autocapitalization)
                .autocorrectionDisabled(isSecure || keyboard == .emailAddress)

                if isSecure {
                    Button { revealed.toggle() } label: {
                        Image(systemName: revealed ? "eye.slash" : "eye").foregroundStyle(Synap.textMuted)
                    }
                    .accessibilityLabel(L(revealed ? "cd_hide_password" : "cd_show_password"))
                }
            }
            .padding(.horizontal, Synap.spaceLg)
            .frame(minHeight: 52)
            .background(Synap.surface)
            .overlay(
                RoundedRectangle(cornerRadius: Synap.radiusMd, style: .continuous)
                    .stroke(error == nil ? Synap.outline : Synap.error, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: Synap.radiusMd, style: .continuous))

            if let error {
                Text(error).font(.synapFootnote).foregroundStyle(Synap.error)
            }
        }
    }
}

// MARK: - Status

struct ErrorBanner: View {
    let message: String

    var body: some View {
        HStack(alignment: .top, spacing: Synap.spaceSm) {
            Image(systemName: "exclamationmark.triangle.fill").foregroundStyle(Synap.error)
            Text(message).font(.synapCallout)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(Synap.spaceMd)
        .background(Synap.errorContainer.opacity(0.6))
        .clipShape(RoundedRectangle(cornerRadius: Synap.radiusMd, style: .continuous))
    }
}

struct EmptyStateView: View {
    let systemImage: String
    let title: String
    let message: String
    var action: (title: String, run: () -> Void)?

    var body: some View {
        VStack(spacing: Synap.spaceMd) {
            Image(systemName: systemImage)
                .font(.system(size: 40, weight: .semibold))
                .foregroundStyle(Synap.brandGradient)
            Text(title).font(.synapTitle).multilineTextAlignment(.center)
            Text(message).font(.synapBody).foregroundStyle(Synap.textMuted).multilineTextAlignment(.center)
            if let action {
                Button(action.title, action: action.run).buttonStyle(SecondaryButtonStyle()).frame(maxWidth: 240)
            }
        }
        .padding(Synap.spaceXl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct PremiumBadge: View {
    var body: some View {
        Label(L("plan_premium"), systemImage: "crown.fill")
            .font(.synapCaption)
            .foregroundStyle(Synap.onPremiumGold)
            .padding(.horizontal, Synap.spaceSm)
            .padding(.vertical, 4)
            .background(Synap.premiumGradient)
            .clipShape(Capsule())
    }
}

/// The agent's in-flight phase: routing → searching → thinking.
struct AgentStatusView: View {
    let status: String

    private var steps: [(key: String, statuses: Set<String>)] {
        [
            ("agent_step_analyze", ["routing"]),
            ("agent_step_search", ["searching"]),
            ("agent_step_compare", ["thinking"]),
        ]
    }

    private var currentIndex: Int {
        steps.firstIndex { $0.statuses.contains(status) } ?? 0
    }

    var body: some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                HStack(spacing: Synap.spaceSm) {
                    if index < currentIndex {
                        Image(systemName: "checkmark.circle.fill").foregroundStyle(Synap.primary)
                    } else if index == currentIndex {
                        ProgressView().controlSize(.small).tint(Synap.primary)
                    } else {
                        Image(systemName: "circle").foregroundStyle(Synap.outlineStrong)
                    }
                    Text(L(step.key))
                        .font(.synapCallout)
                        .foregroundStyle(index <= currentIndex ? Synap.text : Synap.textMuted)
                }
            }
        }
        .synapCard(padding: Synap.spaceMd)
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Products

struct ProductThumbnail: View {
    let url: String?
    var size: CGFloat = 72

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { phase in
            switch phase {
            case let .success(image): image.resizable().scaledToFit()
            default: Image(systemName: "bag").font(.title2).foregroundStyle(Synap.textMuted)
            }
        }
        .frame(width: size, height: size)
        .background(Color.white.opacity(0.95))
        .clipShape(RoundedRectangle(cornerRadius: Synap.radiusMd, style: .continuous))
    }
}

struct ProductCard: View {
    let product: ProductDTO
    let currency: String
    let isFavorite: Bool
    var hasAlert = false
    var onFavorite: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            ZStack(alignment: .topTrailing) {
                ProductThumbnail(url: product.thumbnail, size: 132)
                    .frame(maxWidth: .infinity)
                Button(action: onFavorite) {
                    Image(systemName: isFavorite ? "heart.fill" : "heart")
                        .foregroundStyle(isFavorite ? Synap.error : Synap.textMuted)
                        .padding(8)
                        .background(Synap.bg.opacity(0.7))
                        .clipShape(Circle())
                }
                .accessibilityLabel(L(isFavorite ? "cd_remove_favorite" : "cd_add_favorite"))
            }
            Text(product.title ?? L("product_unknown"))
                .font(.synapCallout)
                .lineLimit(2)
                .frame(minHeight: 40, alignment: .top)
            HStack {
                Text(Formatters.price(product.priceValue, currency: currency) ?? L("product_price_unknown"))
                    .font(.synapHeadline)
                    .foregroundStyle(Synap.primary)
                if hasAlert {
                    Image(systemName: "bell.fill").font(.caption).foregroundStyle(Synap.premiumGold)
                        .accessibilityLabel(L("cd_price_alert_on"))
                }
            }
            Text(product.source ?? L("product_unknown_store"))
                .font(.synapFootnote)
                .foregroundStyle(Synap.textMuted)
                .lineLimit(1)
        }
        .frame(width: 170)
        .synapCard(padding: Synap.spaceMd)
    }
}

// MARK: - Chat

/// Renders the assistant's markdown subset (**bold**, bullets) with AttributedString.
struct MarkdownText: View {
    let text: String

    var body: some View {
        let normalized = text
            .split(separator: "\n", omittingEmptySubsequences: false)
            .map { line -> String in
                var l = String(line)
                if l.hasPrefix("## ") || l.hasPrefix("# ") { l = "**" + l.drop(while: { $0 == "#" }).trimmingCharacters(in: .whitespaces) + "**" }
                if l.hasPrefix("- ") || l.hasPrefix("* ") { l = "• " + l.dropFirst(2) }
                return l
            }
            .joined(separator: "\n")
        let attributed = (try? AttributedString(
            markdown: normalized,
            options: .init(interpretedSyntax: .inlineOnlyPreservingWhitespace)
        )) ?? AttributedString(normalized)
        Text(attributed).font(.synapBody)
    }
}

struct ChatBubble: View {
    let text: String
    let isUser: Bool

    var body: some View {
        HStack {
            if isUser { Spacer(minLength: 48) }
            Group {
                if isUser { Text(text).font(.synapBody) } else { MarkdownText(text: text) }
            }
            .padding(.horizontal, Synap.spaceLg)
            .padding(.vertical, Synap.spaceMd)
            .foregroundStyle(isUser ? Synap.onPrimary : Synap.text)
            .background(isUser ? AnyShapeStyle(Synap.primary) : AnyShapeStyle(Synap.surface))
            .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous)
                    .stroke(isUser ? Color.clear : Synap.outline, lineWidth: 1)
            )
            .textSelection(.enabled)
            if !isUser { Spacer(minLength: 24) }
        }
    }
}

// MARK: - Web

/// In-app Safari for store pages and legal links.
struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let controller = SFSafariViewController(url: url)
        controller.preferredControlTintColor = UIColor(Synap.primary)
        controller.preferredBarTintColor = UIColor(Synap.bg)
        return controller
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}

/// Identifiable wrapper so a URL can drive `.sheet(item:)`.
struct WebLink: Identifiable {
    let url: URL
    var id: String { url.absoluteString }
}

// MARK: - Toast

struct ToastView: View {
    let toast: Toast

    var body: some View {
        HStack(spacing: Synap.spaceSm) {
            Image(systemName: toast.isError ? "exclamationmark.circle.fill" : "checkmark.circle.fill")
                .foregroundStyle(toast.isError ? Synap.error : Synap.primary)
            Text(toast.message).font(.synapCallout).foregroundStyle(Synap.text)
        }
        .padding(.horizontal, Synap.spaceLg)
        .padding(.vertical, Synap.spaceMd)
        .background(Synap.surfaceHighest)
        .clipShape(Capsule())
        .shadow(color: .black.opacity(0.4), radius: 12, y: 4)
        .padding(.horizontal, Synap.screenPadding)
    }
}
