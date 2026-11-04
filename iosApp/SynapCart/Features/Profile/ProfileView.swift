import Shared
import StoreKit
import SwiftUI

struct ProfileView: View {
    @Environment(AppState.self) private var app
    @State private var profile: UserProfile?
    @State private var name = ""
    @State private var language = "tr"
    @State private var currency = "TRY"
    @State private var isSaving = false
    @State private var web: WebLink?
    @State private var showPassword = false
    @State private var showDelete = false
    @State private var showManage = false

    private let currencies = ["TRY", "USD", "EUR", "GBP"]

    var body: some View {
        Form {
            Section { planCard }
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets())

            Section(L("profile_section_settings")) {
                TextField(L("field_full_name"), text: $name).textContentType(.name)
                Picker(L("settings_language"), selection: $language) {
                    Text(L("language_turkish")).tag("tr")
                    Text(L("language_english")).tag("en")
                }
                Picker(L("settings_currency"), selection: $currency) {
                    ForEach(currencies, id: \.self) { Text($0).tag($0) }
                }
                Button(L("action_save_changes")) { save() }
                    .disabled(isSaving || profile == nil)
                VStack(alignment: .leading, spacing: 4) {
                    Text(L("settings_app_language_note")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
                    Button(L("settings_open_ios")) {
                        if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
                    }
                    .font(.synapFootnote)
                }
            }

            Section(L("profile_section_account")) {
                if let email = profile?.email {
                    LabeledContent(L("field_email"), value: email)
                }
                Button(L("profile_change_password")) { showPassword = true }
                Button(L("notifications_enable")) { Task { await app.push.requestAuthorization() } }
                Button(L("action_logout")) { Task { await app.logout() } }
                Button(L("profile_delete_account"), role: .destructive) { showDelete = true }
            }

            Section(L("profile_section_about")) {
                Button(L("legal_privacy")) { web = WebLink(url: AppConfig.privacyURL) }
                Button(L("legal_terms")) { web = WebLink(url: AppConfig.termsURL) }
                Button(L("profile_support")) { web = WebLink(url: AppConfig.supportURL) }
                Text(L("profile_version", AppConfig.appVersion)).foregroundStyle(Synap.textMuted)
            }
        }
        .scrollContentBackground(.hidden)
        .synapScreen()
        .navigationTitle(L("profile_title"))
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
        .sheet(item: $web) { SafariView(url: $0.url).ignoresSafeArea() }
        .sheet(isPresented: $showPassword) { ChangePasswordSheet() }
        .sheet(isPresented: $showDelete) { DeleteAccountSheet() }
        .manageSubscriptionsSheet(isPresented: $showManage)
    }

    private var planCard: some View {
        VStack(alignment: .leading, spacing: Synap.spaceSm) {
            HStack {
                Text(L(app.isPremium ? "plan_card_premium_title" : "plan_card_free_title")).font(.synapHeadline)
                Spacer()
                if app.isPremium { PremiumBadge() }
            }
            Text(L(app.isPremium ? "plan_card_premium_body" : "plan_card_free_body"))
                .font(.synapCallout).foregroundStyle(Synap.textMuted)
            if app.isPremium {
                if let expiry = Formatters.timestamp(app.plan?.expiresAt) {
                    Text(L("plan_card_renews", Formatters.mediumDate(expiry))).font(.synapFootnote).foregroundStyle(Synap.textMuted)
                }
                if app.planSource == "google" {
                    Text(L("paywall_managed_google")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
                } else {
                    Button(L("plan_manage")) { showManage = true }.buttonStyle(SecondaryButtonStyle())
                }
            } else {
                Button(L("action_upgrade_premium")) { app.showPaywall = true }.buttonStyle(PremiumButtonStyle())
            }
        }
        .synapCard()
    }

    private func load() async {
        do {
            let loaded = try await app.client.profile()
            profile = loaded
            name = loaded.fullName ?? ""
            language = loaded.language ?? "tr"
            currency = loaded.currency ?? "TRY"
        } catch {
            app.handle(error, fallback: "error_profile_load")
        }
    }

    private func save() {
        isSaving = true
        Task {
            defer { isSaving = false }
            do {
                profile = try await app.client.updateProfile(fullName: name, language: language, currency: currency)
                await app.refreshProfile()
                app.show(L("profile_saved"))
            } catch {
                app.handle(error, fallback: "error_profile_update")
            }
        }
    }
}

struct ChangePasswordSheet: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    @State private var current = ""
    @State private var new = ""
    @State private var confirm = ""
    @State private var isSaving = false
    @State private var error: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: Synap.spaceLg) {
                    Text(L("password_change_message")).font(.synapCallout).foregroundStyle(Synap.textMuted)
                    if let error { ErrorBanner(message: error) }
                    SynapTextField(title: L("field_current_password"), text: $current, isSecure: true, contentType: .password)
                    SynapTextField(title: L("field_new_password"), text: $new, isSecure: true, contentType: .newPassword,
                                   error: new.isEmpty ? nil : PasswordRules.problem(new))
                    SynapTextField(title: L("field_confirm_password"), text: $confirm, isSecure: true, contentType: .newPassword,
                                   error: !confirm.isEmpty && confirm != new ? L("error_passwords_mismatch") : nil)
                    Button(L("action_save")) { save() }
                        .buttonStyle(PrimaryButtonStyle(isLoading: isSaving))
                        .disabled(isSaving || new != confirm)
                }
                .padding(Synap.screenPadding)
            }
            .synapScreen()
            .navigationTitle(L("profile_change_password"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button(L("action_cancel")) { dismiss() } } }
        }
    }

    private func save() {
        isSaving = true
        error = nil
        Task {
            defer { isSaving = false }
            do {
                _ = try await app.client.changePassword(currentPassword: current, newPassword: new)
                app.show(L("password_changed"))
                dismiss()
            } catch {
                self.error = error.userMessage(fallback: "error_password_update_failed")
            }
        }
    }
}

/// In-app account deletion (App Store Review Guideline 5.1.1(v)).
struct DeleteAccountSheet: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    @State private var password = ""
    @State private var isDeleting = false
    @State private var error: String?
    @State private var showManage = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: Synap.spaceLg) {
                    Text(L("delete_account_message")).font(.synapCallout)
                    if app.isPremium && app.planSource != "google" {
                        VStack(alignment: .leading, spacing: Synap.spaceSm) {
                            Text(L("delete_account_subscription_note")).font(.synapCallout).foregroundStyle(Synap.warning)
                            Button(L("plan_manage")) { showManage = true }.font(.synapCallout)
                        }
                        .synapCard()
                    }
                    if let error { ErrorBanner(message: error) }
                    SynapTextField(title: L("field_password"), text: $password, isSecure: true, contentType: .password)
                    Button(L("delete_account_confirm")) { delete() }
                        .buttonStyle(SecondaryButtonStyle(tint: Synap.error))
                        .disabled(isDeleting)
                }
                .padding(Synap.screenPadding)
            }
            .synapScreen()
            .navigationTitle(L("profile_delete_account"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button(L("action_cancel")) { dismiss() } } }
        }
        .manageSubscriptionsSheet(isPresented: $showManage)
    }

    private func delete() {
        guard !password.isEmpty else {
            error = L("error_delete_needs_password")
            return
        }
        isDeleting = true
        error = nil
        Task {
            defer { isDeleting = false }
            do {
                _ = try await app.client.deleteAccount(password: password)
                dismiss()
                app.accountDeleted()
            } catch {
                self.error = error.userMessage(fallback: "error_account_delete")
            }
        }
    }
}
