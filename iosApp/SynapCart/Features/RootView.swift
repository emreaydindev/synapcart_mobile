import SwiftUI

struct RootView: View {
    @Environment(AppState.self) private var app

    var body: some View {
        @Bindable var app = app
        ZStack {
            switch app.route {
            case .launching:
                SplashView()
            case .onboarding:
                OnboardingView()
            case .auth:
                AuthFlowView()
            case .main:
                MainView()
            }
        }
        .animation(.easeInOut(duration: 0.25), value: app.route)
        .overlay(alignment: .bottom) {
            if let toast = app.toast {
                ToastView(toast: toast)
                    .padding(.bottom, 90)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onTapGesture { app.toast = nil }
            }
        }
        .sheet(isPresented: $app.showPaywall) {
            PaywallView()
        }
        .sheet(item: Binding(
            get: { app.resetToken.map(ResetTokenItem.init) },
            set: { app.resetToken = $0?.token }
        )) { item in
            NavigationStack { ResetPasswordView(token: item.token) }
        }
    }
}

private struct ResetTokenItem: Identifiable {
    let token: String
    var id: String { token }
}

struct SplashView: View {
    var body: some View {
        VStack(spacing: Synap.spaceLg) {
            Image("LogoMark").resizable().scaledToFit().frame(width: 96, height: 96)
            ProgressView().tint(Synap.primary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .synapScreen()
    }
}
