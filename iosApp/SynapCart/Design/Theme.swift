import SwiftUI

/// The SynapCart palette (mirrors Android `SynapColors` and the web tokens). Dark-only.
enum Synap {
    static let bg = Color(hex: 0x060B14)
    static let surface = Color(hex: 0x0E1624)
    static let surfaceHigh = Color(hex: 0x152034)
    static let surfaceHighest = Color(hex: 0x1B2840)
    static let outline = Color(hex: 0x22304A)
    static let outlineStrong = Color(hex: 0x34466A)

    static let primary = Color(hex: 0x2DD4B4)
    static let onPrimary = Color(hex: 0x032A23)
    static let primaryContainer = Color(hex: 0x0B3B33)
    static let secondary = Color(hex: 0x33D6FF)

    static let error = Color(hex: 0xF87171)
    static let errorContainer = Color(hex: 0x4A1515)
    static let warning = Color(hex: 0xF59E0B)
    static let premiumGold = Color(hex: 0xF5C451)
    static let onPremiumGold = Color(hex: 0x2E2205)

    static let text = Color(hex: 0xE8EEF8)
    static let textMuted = Color(hex: 0x9AA8C1)

    static let brandGradient = LinearGradient(colors: [primary, secondary], startPoint: .topLeading, endPoint: .bottomTrailing)
    static let premiumGradient = LinearGradient(colors: [premiumGold, warning], startPoint: .topLeading, endPoint: .bottomTrailing)

    // Spacing and shape (Android `Dimens` / `Shape`).
    static let spaceXs: CGFloat = 4
    static let spaceSm: CGFloat = 8
    static let spaceMd: CGFloat = 12
    static let spaceLg: CGFloat = 16
    static let spaceXl: CGFloat = 24
    static let screenPadding: CGFloat = 20
    static let radiusMd: CGFloat = 12
    static let radiusLg: CGFloat = 16
    static let radiusXl: CGFloat = 24
}

extension Color {
    init(hex: UInt32, opacity: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }
}

/// Plus Jakarta Sans (bundled, OFL). Sizes scale with Dynamic Type.
extension Font {
    enum JakartaWeight: String {
        case regular = "PlusJakartaSans-Regular"
        case medium = "PlusJakartaSans-Medium"
        case semibold = "PlusJakartaSans-SemiBold"
        case bold = "PlusJakartaSans-Bold"
        case extraBold = "PlusJakartaSans-ExtraBold"
    }

    static func jakarta(_ size: CGFloat, _ weight: JakartaWeight = .regular, relativeTo style: Font.TextStyle = .body) -> Font {
        .custom(weight.rawValue, size: size, relativeTo: style)
    }

    static let synapDisplay = jakarta(30, .extraBold, relativeTo: .largeTitle)
    static let synapTitle = jakarta(22, .bold, relativeTo: .title2)
    static let synapHeadline = jakarta(17, .semibold, relativeTo: .headline)
    static let synapBody = jakarta(16, .regular, relativeTo: .body)
    static let synapCallout = jakarta(15, .medium, relativeTo: .callout)
    static let synapCaption = jakarta(13, .medium, relativeTo: .caption)
    static let synapFootnote = jakarta(12, .regular, relativeTo: .footnote)
}

// MARK: - Button styles

struct PrimaryButtonStyle: ButtonStyle {
    var isLoading = false
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        ZStack {
            configuration.label.opacity(isLoading ? 0 : 1)
            if isLoading { ProgressView().tint(Synap.onPrimary) }
        }
        .font(.synapHeadline)
        .foregroundStyle(Synap.onPrimary)
        .frame(maxWidth: .infinity, minHeight: 52)
        .background(Synap.brandGradient.opacity(isEnabled ? 1 : 0.4))
        .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous))
        .scaleEffect(configuration.isPressed ? 0.98 : 1)
        .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    var tint: Color = Synap.text

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.synapHeadline)
            .foregroundStyle(tint)
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(Synap.surface)
            .overlay(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous).stroke(Synap.outlineStrong, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous))
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}

struct PremiumButtonStyle: ButtonStyle {
    var isLoading = false

    func makeBody(configuration: Configuration) -> some View {
        ZStack {
            configuration.label.opacity(isLoading ? 0 : 1)
            if isLoading { ProgressView().tint(Synap.onPremiumGold) }
        }
        .font(.synapHeadline)
        .foregroundStyle(Synap.onPremiumGold)
        .frame(maxWidth: .infinity, minHeight: 52)
        .background(Synap.premiumGradient)
        .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous))
        .scaleEffect(configuration.isPressed ? 0.98 : 1)
    }
}

// MARK: - Surfaces

struct CardModifier: ViewModifier {
    var padding: CGFloat = Synap.spaceLg

    func body(content: Content) -> some View {
        content
            .padding(padding)
            .background(Synap.surface)
            .overlay(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous).stroke(Synap.outline, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: Synap.radiusLg, style: .continuous))
    }
}

extension View {
    func synapCard(padding: CGFloat = Synap.spaceLg) -> some View { modifier(CardModifier(padding: padding)) }

    /// The app background behind every screen.
    func synapScreen() -> some View {
        background(Synap.bg.ignoresSafeArea())
            .foregroundStyle(Synap.text)
            .toolbarBackground(Synap.bg, for: .navigationBar)
            .toolbarColorScheme(.dark, for: .navigationBar)
    }
}
