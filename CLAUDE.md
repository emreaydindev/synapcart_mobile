# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SynapCart Android app: a Jetpack Compose client for the SynapCart AI shopping assistant. It talks to the FastAPI backend (`synapcart_backend`). The repos are grouped as submodules in the `synapcart` umbrella repo.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests
./gradlew connectedAndroidTest          # instrumented tests (needs device)
./gradlew test --tests "com.neilb.synapcart.ExampleUnitTest"   # single test class
./gradlew lint
```

Only the template example tests exist. The build uses a version catalog (`gradle/libs.versions.toml`), AGP 9, KSP for Hilt, compileSdk/targetSdk 36, minSdk 24 and Java 11.

## Backend URL

`util/Constants.BASE_URL` points to the production Render backend (`https://synapcart-backend.onrender.com/`). For local development, change it to `http://10.0.2.2:8001/` (emulator) or your machine's address. The trailing slash is required by Retrofit and by the manual URL build in `ChatStreamRepository`. Cleartext traffic is enabled in the manifest.

## Architecture

Clean-architecture style MVVM under `app/src/main/java/com/neilb/synapcart/`:

- `data/remote/*ApiService`: Retrofit interfaces split by backend router: auth, chat (`SynapCartApiService`), user, favorites. `data/model` holds the DTOs that mirror the backend's JSON.
- `data/repository/*Impl`: wraps calls in `kotlin.Result`. Each one parses the backend's `{"detail": ...}` error body (`ErrorResponse`) into the exception message and falls back to a Turkish generic message.
- `domain/repository` holds the interfaces. `domain/use_case/*` contains one class per action, grouped into `*UseCases` aggregate data classes. ViewModels inject the aggregate. Note that `ForgotPasswordUseCase` lives in the `chat` package even though it belongs to auth.
- `di/AppModule.kt` wires repositories and use cases **manually** with `@Provides`, not with `@Binds` or `@Inject` constructors. A new use case must be added both to its `*UseCases` class and to the provider. `di/NetworkModule.kt` provides OkHttp (auth interceptor, BODY logging, long read timeout for the agent), Retrofit and the API services.
- **Auth:** `util/SessionManager` stores the JWT, user name and onboarding flag in Preferences DataStore. `AuthInterceptor` adds `Authorization: Bearer` to every request except paths that contain `/auth/`.
- **Chat streaming:** the chat does not go through Retrofit. `ChatStreamRepository` uses raw OkHttp to POST `api/v1/chat/agent/{sessionId}/stream` and parses each SSE `data:` line into `AgentStreamEvent` (`status`, `analysis`, `products`). `DashboardViewModel` consumes this flow. Stay in sync with the backend's `stream_agent_invoke` payload shape.
- **Navigation:** `ui/navigation/Screen.kt` defines routes with query-arg helpers. Products are passed to `ProductDetails` as URI-encoded JSON, and `ProductWeb` takes a URL for an in-app WebView. `SynapCartNavHost` routes Splash to Onboarding, Login or Dashboard based on the session state.
- **Deep links:** `ResetPassword` accepts `https://synapcart.app/reset-password?token=…` and `synapcart://reset-password?token=…`. These are declared both in `AndroidManifest.xml` intent filters and in the `navDeepLink`s in the NavHost.
- `util/SnackbarController` is an app-wide singleton for snackbar events. `ui/components` and `ui/theme` hold the shared dark/neon design system.

User-facing strings are mostly hard-coded Turkish.
