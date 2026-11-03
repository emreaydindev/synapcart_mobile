# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SynapCart Android app: a Jetpack Compose client for the SynapCart AI shopping assistant. It talks to the FastAPI backend (`synapcart_backend`). The repos are grouped as submodules in the `synapcart` umbrella repo.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests
./gradlew connectedAndroidTest          # instrumented tests (needs device)
./gradlew test --tests "com.neilb.synapcart.ChatStreamRepositoryTest"   # single test class
./gradlew assembleRelease               # R8-minified release (signed if keystore.properties exists)
./gradlew lint
```

JVM tests live in `app/src/test`: `UtilTest` covers password rules, error parsing, timestamps and price formatting, and `ChatStreamRepositoryTest` runs the SSE client against OkHttp `MockWebServer`. The build uses a version catalog (`gradle/libs.versions.toml`), AGP 9, KSP for Hilt, compileSdk 36.1, targetSdk 36, minSdk 24 and Java 11, with core-library desugaring so `java.time` works on API 24–25.

## Build config and release

- `BuildConfig.BASE_URL` is set per build type in `app/build.gradle.kts`. Debug uses `http://10.0.2.2:8001/` (the emulator's host loopback); override it with `./gradlew installDebug -PdebugBaseUrl=http://192.168.1.10:8001/`. Release uses the Render backend. Keep the trailing slash.
- `BuildConfig.WEB_URL` is the web portal, used for the privacy, terms and support links.
- Cleartext HTTP is allowed only in debug, and only to `10.0.2.2` and `localhost` (`src/debug/res/xml/network_security_config.xml`). Release is HTTPS-only.
- Release builds use `isMinifyEnabled` and `isShrinkResources`. `proguard-rules.pro` keeps `data/model/**`, because Gson reflects over the DTOs. New DTOs must stay in that package.
- Release signing reads `keystore.properties` from the project root, which is gitignored and holds `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. Without it the release APK is unsigned.
- `allowBackup=false`, and the backup rules exclude DataStore, so the token never leaves the device.

## Architecture

Clean-architecture style MVVM under `app/src/main/java/com/neilb/synapcart/`:

- `data/remote/*ApiService`: Retrofit interfaces split by backend router: auth, chat (`SynapCartApiService`), user, favorites. `data/model` holds the DTOs that mirror the backend's JSON.
- `data/repository/*Impl` wrap every call in `safeApiCall {}` (`data/remote/ApiCall.kt`), which returns `kotlin.Result`. Failures become typed `ApiException`s (`Network`, `Unauthorized`, `QuotaExceeded` (402), `RateLimited` (429), `Http`) whose `message` is the backend's `detail` string or a Turkish fallback, so ViewModels can show `e.message` directly. For endpoints that return 204, declare `Response<Unit>` and call `.requireSuccess()` inside `safeApiCall`.
- `FavoritesRepository` keeps a shared in-memory cache (`favorites`, `favoriteLinks`), so heart icons are correct on every screen. `toggle(product)` adds the favorite, or removes it with `DELETE /favorites/by-link`.
- `domain/repository` holds the interfaces. `domain/use_case/*` contains one class per action, grouped into `*UseCases` aggregate data classes. ViewModels inject the aggregate.
- `di/AppModule.kt` wires repositories and use cases **manually** with `@Provides`, not with `@Binds` or `@Inject` constructors. A new use case must be added both to its `*UseCases` class and to the provider. `di/NetworkModule.kt` provides OkHttp, Retrofit and the API services. OkHttp has the auth interceptor, a long read timeout for the agent, and BODY logging in debug only, with `Authorization` redacted.
- **Auth and session:**
  - `util/SessionManager` stores the JWT, user name, language, currency and the onboarding flag in Preferences DataStore. `clearSession()` keeps the onboarding flag and language.
  - `AuthInterceptor` adds `Authorization: Bearer` to every request except `/auth/` paths. When an authenticated request returns 401 (an expired token, or one revoked by a password change), it clears the session and fires `SessionEvents.expired`. `SynapCartNavHost` collects that event and navigates to Login. A 401 from `/auth/login` just means a wrong password.
  - `POST /auth/register` returns the user, not a token. `AuthRepository.register` therefore logs in right after.
- **Chat streaming:**
  - The chat does not go through Retrofit. `ChatStreamRepository` uses raw OkHttp to POST `api/v1/chat/agent/{sessionId}/stream` and parses each SSE `data:` line into `AgentStreamEvent` (`status`, `analysis`, `products`).
  - The status sequence is `routing` → `searching` → `thinking` (+products) → `completed` (+analysis, products). It can end instead with `out_of_scope` or `error`, each with an analysis.
  - The flow ends after a final event, throws `ApiException` on HTTP or network errors, and cancels the call when the collector is cancelled.
  - `DashboardViewModel` turns the in-flight state into `AgentPhase` and `pendingProducts`, then appends the answer as a `ChatMessage` with its `products`. History from `/messages` restores those products too.
  - Keep in sync with the backend's `stream_agent()`.
- **Navigation:** `ui/navigation/Screen.kt` defines routes with query-arg helpers. Products reach `ProductDetails` through the `SelectedProductStore` singleton, not the route; the screen closes if the store is empty after process death. `ProductWeb` takes a URL for an in-app WebView. `SynapCartNavHost` routes Splash to Onboarding, Login or Dashboard based on the session state.
- **Deep links:** `ResetPassword` accepts `https://synapcart.app/reset-password?token=…` and `synapcart://reset-password?token=…`. These are declared both in `AndroidManifest.xml` intent filters and in the `navDeepLink`s in the NavHost.
- `util/SnackbarController` is an app-wide singleton for snackbar events. `ui/components` and `ui/theme` hold the shared dark/neon design system.
- **Formatting:** `util/Formatters.kt` has `parseServerTimestamp` (the backend sends naive UTC) and `formatPrice(amount, currency)`. Products are searched in the user's currency, so format them with `SessionManager.currency`. `util/PasswordRules` mirrors the backend's password rules.

User-facing strings are mostly hard-coded Turkish.
