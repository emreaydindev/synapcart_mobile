import SwiftUI

struct AuthFlowView: View {
    var body: some View {
        NavigationStack { LoginView() }
    }
}

/// Shared layout: logo, title, subtitle and a scrollable form.
private struct AuthLayout<Content: View>: View {
    let title: String
    let subtitle: String?
    @ViewBuilder let content: Content

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Synap.spaceXl) {
                LogoLockup().padding(.top, Synap.spaceXl)
                VStack(alignment: .leading, spacing: Synap.spaceSm) {
                    Text(title).font(.synapDisplay)
                    if let subtitle {
                        Text(subtitle).font(.synapBody).foregroundStyle(Synap.textMuted)
                    }
                }
                content
            }
            .padding(Synap.screenPadding)
            .frame(maxWidth: 520)
            .frame(maxWidth: .infinity)
        }
        .scrollDismissesKeyboard(.interactively)
        .synapScreen()
    }
}

struct LoginView: View {
    @Environment(AppState.self) private var app
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var error: String?

    var body: some View {
        AuthLayout(title: L("login_submit"), subtitle: L("brand_tagline")) {
            VStack(spacing: Synap.spaceLg) {
                if let error { ErrorBanner(message: error) }
                SynapTextField(title: L("field_email"), text: $email, contentType: .username,
                               keyboard: .emailAddress, autocapitalization: .never)
                SynapTextField(title: L("field_password"), text: $password, isSecure: true, contentType: .password)
                HStack {
                    Spacer()
                    NavigationLink(L("login_forgot_password")) { ForgotPasswordView(prefill: email) }
                        .font(.synapCallout)
                }
                Button(L("login_submit")) { submit() }
                    .buttonStyle(PrimaryButtonStyle(isLoading: isLoading))
                    .disabled(isLoading)
                HStack(spacing: Synap.spaceXs) {
                    Text(L("login_no_account")).foregroundStyle(Synap.textMuted)
                    NavigationLink(L("login_go_register")) { RegisterView() }
                }
                .font(.synapCallout)
            }
        }
        .onSubmit(submit)
    }

    private func submit() {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        Task {
            defer { isLoading = false }
            do {
                _ = try await app.client.login(email: email, password: password)
                await app.enterMain()
            } catch {
                self.error = error.userMessage(fallback: "error_login_failed")
            }
        }
    }
}

struct RegisterView: View {
    @Environment(AppState.self) private var app
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var error: String?
    @State private var link: WebLink?

    private var passwordProblem: String? { password.isEmpty ? nil : PasswordRules.problem(password) }

    var body: some View {
        AuthLayout(title: L("register_title"), subtitle: L("register_subtitle")) {
            VStack(spacing: Synap.spaceLg) {
                if let error { ErrorBanner(message: error) }
                SynapTextField(title: L("field_full_name"), text: $name, contentType: .name, autocapitalization: .words)
                SynapTextField(title: L("field_email"), text: $email, contentType: .username,
                               keyboard: .emailAddress, autocapitalization: .never)
                SynapTextField(title: L("field_password"), text: $password, isSecure: true,
                               contentType: .newPassword, error: passwordProblem)
                if passwordProblem == nil {
                    Text(L("password_hint")).font(.synapFootnote).foregroundStyle(Synap.textMuted)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                Button(L("register_submit")) { submit() }
                    .buttonStyle(PrimaryButtonStyle(isLoading: isLoading))
                    .disabled(isLoading)
                consent
            }
        }
        .sheet(item: $link) { SafariView(url: $0.url).ignoresSafeArea() }
    }

    private var consent: some View {
        VStack(spacing: Synap.spaceXs) {
            Text(L("register_consent", L("legal_terms"), L("legal_privacy")))
                .font(.synapFootnote)
                .foregroundStyle(Synap.textMuted)
                .multilineTextAlignment(.center)
            HStack(spacing: Synap.spaceLg) {
                Button(L("legal_terms")) { link = WebLink(url: AppConfig.termsURL) }
                Button(L("legal_privacy")) { link = WebLink(url: AppConfig.privacyURL) }
            }
            .font(.synapCaption)
        }
    }

    private func submit() {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        Task {
            defer { isLoading = false }
            do {
                _ = try await app.client.register(fullName: name, email: email, password: password)
                await app.enterMain()
            } catch {
                self.error = error.userMessage(fallback: "error_register_failed")
            }
        }
    }
}

struct ForgotPasswordView: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    @State var prefill: String
    @State private var isLoading = false
    @State private var sentTo: String?
    @State private var error: String?

    var body: some View {
        AuthLayout(title: L(sentTo == nil ? "forgot_title" : "forgot_sent_title"),
                   subtitle: sentTo.map { L("forgot_sent_message", $0) } ?? L("forgot_subtitle")) {
            VStack(spacing: Synap.spaceLg) {
                if let error { ErrorBanner(message: error) }
                if sentTo == nil {
                    SynapTextField(title: L("field_email"), text: $prefill, contentType: .username,
                                   keyboard: .emailAddress, autocapitalization: .never)
                    Button(L("forgot_submit")) { submit() }
                        .buttonStyle(PrimaryButtonStyle(isLoading: isLoading))
                        .disabled(isLoading)
                } else {
                    Button(L("forgot_back_to_login")) { dismiss() }
                        .buttonStyle(SecondaryButtonStyle())
                }
            }
        }
    }

    private func submit() {
        isLoading = true
        error = nil
        Task {
            defer { isLoading = false }
            do {
                _ = try await app.client.forgotPassword(email: prefill)
                sentTo = prefill.trimmingCharacters(in: .whitespaces)
            } catch {
                self.error = error.userMessage(fallback: "error_reset_link_failed")
            }
        }
    }
}

/// Opened from a reset link (synapcart://reset-password?token=… or the Universal Link).
struct ResetPasswordView: View {
    @Environment(AppState.self) private var app
    @Environment(\.dismiss) private var dismiss
    let token: String
    @State private var password = ""
    @State private var confirm = ""
    @State private var isLoading = false
    @State private var done = false
    @State private var error: String?

    private var problem: String? { password.isEmpty ? nil : PasswordRules.problem(password) }

    var body: some View {
        AuthLayout(title: L(done ? "reset_done_title" : "reset_title"),
                   subtitle: L(done ? "reset_done_message" : "reset_subtitle")) {
            VStack(spacing: Synap.spaceLg) {
                if let error { ErrorBanner(message: error) }
                if done {
                    Button(L("forgot_back_to_login")) { finish() }.buttonStyle(PrimaryButtonStyle())
                } else {
                    SynapTextField(title: L("field_new_password"), text: $password, isSecure: true,
                                   contentType: .newPassword, error: problem)
                    SynapTextField(title: L("field_confirm_password"), text: $confirm, isSecure: true,
                                   contentType: .newPassword,
                                   error: !confirm.isEmpty && confirm != password ? L("error_passwords_mismatch") : nil)
                    Button(L("reset_submit")) { submit() }
                        .buttonStyle(PrimaryButtonStyle(isLoading: isLoading))
                        .disabled(isLoading || password.isEmpty || password != confirm)
                }
            }
        }
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button(L("action_close")) { dismiss() }
            }
        }
    }

    private func submit() {
        isLoading = true
        error = nil
        Task {
            defer { isLoading = false }
            do {
                _ = try await app.client.resetPassword(token: token, newPassword: password)
                done = true
            } catch {
                self.error = error.userMessage(fallback: "error_reset_link_invalid")
            }
        }
    }

    private func finish() {
        // Every session was revoked by the reset; sign in again with the new password.
        Task {
            if app.route == .main { await app.logout() }
            dismiss()
        }
    }
}
