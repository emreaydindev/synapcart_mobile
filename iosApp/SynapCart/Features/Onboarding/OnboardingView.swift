import SwiftUI

struct OnboardingView: View {
    @Environment(AppState.self) private var app
    @State private var page = 0
    @State private var language = Locale.current.language.languageCode?.identifier == "tr" ? "tr" : "en"
    @State private var currency = Locale.current.currency?.identifier ?? "TRY"

    private let currencies = ["TRY", "USD", "EUR", "GBP"]

    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $page) {
                welcome.tag(0)
                features.tag(1)
                preferences.tag(2)
                premium.tag(3)
            }
            .tabViewStyle(.page(indexDisplayMode: .always))

            VStack(spacing: Synap.spaceMd) {
                if page < 3 {
                    Button(L(page == 2 ? "action_continue" : "onboarding_next")) {
                        withAnimation { page += 1 }
                    }
                    .buttonStyle(PrimaryButtonStyle())
                } else {
                    Button(L("onboarding_start")) { finish() }
                        .buttonStyle(PrimaryButtonStyle())
                }
            }
            .padding(Synap.screenPadding)
        }
        .synapScreen()
        .onAppear {
            if !currencies.contains(currency) { currency = "TRY" }
        }
    }

    private func finish() {
        Task { await app.finishOnboarding(language: language, currency: currency) }
    }

    private var welcome: some View {
        VStack(spacing: Synap.spaceXl) {
            Spacer()
            Image("LogoMark").resizable().scaledToFit().frame(width: 120, height: 120)
            Text(L("onboarding_welcome_title")).font(.synapDisplay)
            Text(L("onboarding_welcome_subtitle"))
                .font(.synapBody)
                .foregroundStyle(Synap.textMuted)
                .multilineTextAlignment(.center)
            Spacer()
        }
        .padding(Synap.screenPadding)
    }

    private var features: some View {
        VStack(alignment: .leading, spacing: Synap.spaceLg) {
            Spacer()
            feature("text.bubble", "onboarding_feature_search_title", "onboarding_feature_search_body")
            feature("sparkles", "onboarding_feature_analysis_title", "onboarding_feature_analysis_body")
            feature("storefront", "onboarding_feature_stores_title", "onboarding_feature_stores_body")
            Spacer()
        }
        .padding(Synap.screenPadding)
    }

    private func feature(_ icon: String, _ title: String, _ body: String) -> some View {
        HStack(alignment: .top, spacing: Synap.spaceLg) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(Synap.brandGradient)
                .frame(width: 44, height: 44)
                .background(Synap.primaryContainer)
                .clipShape(RoundedRectangle(cornerRadius: Synap.radiusMd))
            VStack(alignment: .leading, spacing: Synap.spaceXs) {
                Text(L(title)).font(.synapHeadline)
                Text(L(body)).font(.synapCallout).foregroundStyle(Synap.textMuted)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .synapCard()
    }

    private var preferences: some View {
        VStack(alignment: .leading, spacing: Synap.spaceLg) {
            Spacer()
            Text(L("onboarding_prefs_title")).font(.synapTitle)
            Text(L("onboarding_prefs_subtitle")).font(.synapBody).foregroundStyle(Synap.textMuted)
            VStack(alignment: .leading, spacing: Synap.spaceSm) {
                Text(L("settings_language")).font(.synapCaption).foregroundStyle(Synap.textMuted)
                Picker(L("settings_language"), selection: $language) {
                    Text(L("language_turkish")).tag("tr")
                    Text(L("language_english")).tag("en")
                }
                .pickerStyle(.segmented)
                Text(L("settings_app_language_note")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            }
            VStack(alignment: .leading, spacing: Synap.spaceSm) {
                Text(L("settings_currency")).font(.synapCaption).foregroundStyle(Synap.textMuted)
                Picker(L("settings_currency"), selection: $currency) {
                    ForEach(currencies, id: \.self) { Text($0).tag($0) }
                }
                .pickerStyle(.segmented)
            }
            Text(L("onboarding_prefs_note")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            Spacer()
        }
        .padding(Synap.screenPadding)
    }

    private var premium: some View {
        VStack(alignment: .leading, spacing: Synap.spaceLg) {
            Spacer()
            PremiumBadge()
            Text(L("onboarding_premium_title")).font(.synapTitle)
            Text(L("onboarding_premium_subtitle")).font(.synapBody).foregroundStyle(Synap.textMuted)
            PremiumFeatureList()
            Text(L("onboarding_premium_note")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
            Spacer()
        }
        .padding(Synap.screenPadding)
    }
}

/// The four premium benefits (shared by onboarding and the paywall).
struct PremiumFeatureList: View {
    var body: some View {
        VStack(alignment: .leading, spacing: Synap.spaceMd) {
            row("brain.head.profile", "premium_feature_deep_title", "premium_feature_deep_body")
            row("bell.badge", "premium_feature_alerts_title", "premium_feature_alerts_body")
            row("square.split.2x1", "premium_feature_compare_title", "premium_feature_compare_body")
            row("clock.arrow.circlepath", "premium_feature_history_title", "premium_feature_history_body")
        }
    }

    private func row(_ icon: String, _ title: String, _ body: String) -> some View {
        HStack(alignment: .top, spacing: Synap.spaceMd) {
            Image(systemName: icon).foregroundStyle(Synap.premiumGold).frame(width: 24)
            VStack(alignment: .leading, spacing: 2) {
                Text(L(title)).font(.synapHeadline)
                Text(L(body)).font(.synapCallout).foregroundStyle(Synap.textMuted)
            }
        }
    }
}
