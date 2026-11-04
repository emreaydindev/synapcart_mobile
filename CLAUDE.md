# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SynapCart mobile is a Kotlin Multiplatform project for the SynapCart AI shopping assistant:
- `shared/` is a KMP module with the API client, models, repositories, session storage and business rules;
- `iosApp/` is the SwiftUI iOS app, which ships first;
- `app/` is the Jetpack Compose Android app.

The backend is a Cloudflare Worker (`synapcart_backend`): production `https://api.synapcart.emreaydin.dev/`, staging `https://staging-api.synapcart.emreaydin.dev/`. The web portal is `https://synapcart.emreaydin.dev/`. All repos are submodules of the `synapcart` umbrella repo.

## Commands

```bash
./gradlew :shared:testAndroidHostTest                 # shared tests on the JVM (Ktor MockEngine, in-memory DataStore)
./gradlew :shared:compileKotlinIosSimulatorArm64      # iOS klib; cross-compiles on Linux (linking needs macOS)
./gradlew :shared:iosSimulatorArm64Test               # shared tests on the iOS simulator (macOS only)
./gradlew :app:testDebugUnitTest :app:lintDebug       # Android JVM tests + lint
./gradlew :app:assembleRelease                        # R8 release (signed if keystore.properties exists)
./gradlew :app:assembleStaging                        # release-like build against staging-api
cd iosApp && xcodegen generate                        # (macOS) generates SynapCart.xcodeproj from project.yml
xcodebuild test -project iosApp/SynapCart.xcodeproj -scheme SynapCart -destination 'platform=iOS Simulator,name=iPhone 16'
python3 iosApp/scripts/generate_resources.py ../synapcart/design/logo_foreground.png   # iOS strings/icon/fonts from Android
```

Without Android Studio, set `sdk.dir` in `local.properties` (gitignored). The build uses a version catalog, AGP 9 (built-in Kotlin, `com.android.kotlin.multiplatform.library` for `:shared`), Kotlin 2.3.21, KSP 2.3.x for Hilt, compileSdk 36.1, minSdk 24 and Java 11 with desugaring. **Shared-module libraries (Ktor, kotlinx.serialization, coroutines) must be releases built with Kotlin ≤ 2.3**, otherwise the iOS klibs fail with "incompatible ABI version". That is why the catalog pins `ktor`, `kotlinxSerialization` and `kotlinxCoroutinesKmp` below the latest releases.

## Shared module (`shared/`)

Packages keep the pre-KMP Android names, so Android imports are unchanged.

- **`data/model/Models.kt`:** `@Serializable` DTOs mirroring the backend JSON.
  - Nullable fields default to null.
  - `SynapJson` keeps explicit nulls, so `FavoriteUpdateRequest(null)` sends `target_price: null` to clear an alert.
  - `UserProfile.planSource` is `google` or `apple`.
- **`data/remote/SynapCartApi.kt`:** the Ktor client (OkHttp engine on Android, Darwin on iOS) covering every endpoint.
  - It adds `Authorization: Bearer` except on `auth/` paths.
  - On a 401 to an authenticated call it clears the session and fires `SessionEvents.expired`, unless a fresh login raced it.
  - Non-2xx responses throw `ApiException.fromHttp`.
  - `streamAgent()` posts to `chat/agent/{id}/stream` and parses SSE `data:` lines into `AgentStreamEvent`, stopping after a final status.
  - The request timeout is 120 s.
- **`data/remote/ApiException.kt`:** typed failures `Network(cause)`, `Unauthorized`, `QuotaExceeded(code)` (402: `quota_exceeded` / `premium_required` / `history_locked`), `RateLimited`, `Http(code)` and `Validation(reason)`.
  - `safeApiCall {}` wraps results in `kotlin.Result`.
  - There are no string resources here. Android maps exceptions to text in `app/.../data/remote/ApiErrors.kt` (`uiText`, `toUiText`, `validationRes`); iOS does it in `iosApp/.../Core/Interop.swift` (`userMessage`).
- **`data/repository/*`:**
  - Auth, User, Favorites (the shared in-memory cache behind `favorites` / `favoriteLinks`), SynapCart (sessions, messages, compare) and ChatStream.
  - `PlatformHooks` keeps platform work native: `beforeSignOut` unregisters the FCM token, `onProfileLoaded` applies the Android app locale.
  - `UserRepository.applyProfile` caches a profile returned by store verification.
- **`domain/*`:** repository interfaces, `ChatMessage`, and use cases grouped into `*UseCases`. Validation reasons are the `util/ValidationReasons` constants.
- **`util/SessionManager.kt`:** Preferences DataStore (KMP), created by `createSessionDataStore(path)`.
  - Android keeps the old file `files/datastore/synapcart_prefs.preferences_pb`; iOS uses Documents.
  - Create exactly one instance per process.
  - `SessionEvents` lives in the same file.
- **`util/Rules.kt`:** `PasswordPolicy` (8–72 bytes, letters and digits, mirroring the backend), `parseServerTimestamp`, `parsePriceInput`, `suggestTargetPrice` and `sessionGroupOf` (kotlinx-datetime).
- **`shared/SynapCart.kt`:** the composition root (API, session, plan, repositories).
- **`shared/SynapCartClient.kt`:** the Swift facade.
  - Repositories return `Result` and expose `Flow`s, neither of which exports to Swift, so this class offers `@Throws suspend` functions (Swift `async throws`) and callback-based `watch*`/`streamAgent`, which return a `WatchHandle` to cancel.
  - Callbacks run on `Dispatchers.Main`.
  - It also handles `verifyApplePurchase` and iOS device tokens.
- **`iosMain/.../IosPlatform.kt`:** `createSynapCartClient(baseUrl, hooks, logRequests)` (Swift: `IosPlatformKt.createSynapCartClient`) and the `IosHooks` protocol.
- **Tests:** `commonTest/SharedTest.kt` runs Ktor `MockEngine` with an in-memory DataStore. Use `realTimeTest {}`, not bare `runTest`, because virtual time fires Ktor's `HttpTimeout` instantly.

## iOS app (`iosApp/`)

- **Project:** XcodeGen `project.yml`; `SynapCart.xcodeproj` is generated and gitignored.
  - Configurations: Debug (`http://localhost:8001/`), Staging (`staging-api`), Release (`api.`). Values come from `Configs/*.xcconfig` → Info.plist (`SynapAPIBaseURL`, `SynapWebURL`, `SynapPremium*`) → `AppConfig.swift`.
  - A pre-build script runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`; the Staging configuration sets `KOTLIN_FRAMEWORK_BUILD_TYPE=release`.
  - Firebase Messaging comes from SPM.
- **`App/AppState.swift`:** `@Observable` root state.
  - It holds the route (launching / onboarding / auth / main), toasts, the paywall flag, deep-link reset tokens, a pending product link from push, and mirrors of the plan, favorites and currency.
  - Mirrored values come from `client.watch*`.
  - `handle(error)` opens the paywall on a 402 unless the user is premium.
- **`App/StoreKitManager.swift`:** StoreKit 2 for the group `synapcart_premium` (`synapcart_premium_monthly`/`_yearly`).
  - Purchases set `appAccountToken` to the user's UUID.
  - Every verified transaction (purchase, `Transaction.updates`, `currentEntitlements` at sign-in, restore via `AppStore.sync`) goes to `client.verifyApplePurchase(jws)`. The transaction is `finish()`ed only after the server accepts it.
  - `Resources/Configuration.storekit` is attached to the Debug run scheme.
- **`App/PushManager.swift`:**
  - Firebase is configured only if `GoogleService-Info.plist` is bundled; otherwise push is a no-op.
  - The APNs token is passed to FCM, and the FCM token is sent via `registerDeviceToken` with `platform=ios`.
  - Permission is asked once after the first favorite and again when a price alert is set.
  - Tapping a notification reads the `link` data and opens the product.
- **Features:**
  - Onboarding (language and currency saved to the profile);
  - Auth: login, register, forgot password, and reset (`synapcart://reset-password?token=` and the Universal Link `https://synapcart.emreaydin.dev/reset-password?token=`);
  - Chat (`ChatViewModel`: SSE stream, agent steps, product rows, quota banner, drawer grouped by day with rename, delete and locked chats);
  - Product detail (SFSafariViewController, favorite, price-alert sheet);
  - Favorites (select up to 4 to compare), Compare, Paywall (the terms Apple requires, EULA and privacy links, restore, manage subscription);
  - Profile (preferences, change password, in-app account deletion, legal links).
- **Strings:** `Resources/Localizable.xcstrings` is generated from the Android `strings.xml` (Turkish source plus English) by `scripts/generate_resources.py`. iOS-only strings live in that script's `IOS_STRINGS`. Swift uses `L("key")`, `L("key", args…)` and `LP("plural", count:, args…)`, and the keys are the Android resource names. The UI language follows iOS per-app settings; the profile language drives assistant replies and emails.
- **Design:** `Design/Theme.swift` holds the `Synap` colors (same tokens as Android `SynapColors` and the web), Plus Jakarta Sans and button styles; `Design/Components.swift` holds the shared views.
- **Release:**
  - `Resources/PrivacyInfo.xcprivacy` and `SynapCart.entitlements` (push, associated domains).
  - `fastlane/Fastfile` has `beta_staging`, `beta` and `release` lanes, authenticated with an App Store Connect API key.
  - The steps are in `docs/IOS_RELEASE.md`.

## Android app (`app/`)

- **Build config:** `BuildConfig.BASE_URL` / `WEB_URL` per build type.
  - debug: `http://10.0.2.2:8001/`, override with `-PdebugBaseUrl=`;
  - staging: `staging-api` / `staging.`;
  - release: `api.` / the main site.
  - Keep the trailing slash.
  - Cleartext is allowed only in debug.
- **DI:** Hilt `di/NetworkModule` builds the shared `SynapCart`. It uses the DataStore path above, `PlatformHooks` (a `PushRepository` provider and `AppLocale`), and debug-only request logging with Authorization redacted. It exposes `SessionManager`, `SessionEvents` and `SynapCartApi`. `di/AppModule` provides the repositories from it and wires the use cases manually (`@Provides`). A new use case must be added both to its `*UseCases` class and to the provider.
- **UI:** Compose screens, ViewModels and the `ui/theme` + `ui/components` design system are unchanged. ViewModels show `e.toUiText(fallback)`; never `e.message`.
- **Billing** (`data/billing/BillingRepository`, Play Billing 9):
  - The `obfuscatedAccountId` is the user id.
  - Each purchase goes to `SynapCartApi.verifyGooglePurchase`, then is acknowledged, then `/user/me` is refreshed.
  - The product and base-plan ids in `app/build.gradle.kts` are placeholders.
- **Push** (`data/push`): FCM. The `google-services` plugin is applied only when `app/google-services.json` exists; device tokens are registered with `platform=android`.
- **Deep links:** `https://synapcart.emreaydin.dev/reset-password?token=…` (App Links, verified by the web's `assetlinks.json`) and `synapcart://reset-password?token=…`, declared in `AndroidManifest.xml` and in the NavHost `navDeepLink`s.
- **Release:** `proguard-rules.pro` only needs line-number rules, because kotlinx.serialization, Ktor and DataStore ship consumer rules. Release signing comes from `keystore.properties`.
- **Tests:** `app/src/test` holds `PremiumLogicTest`, `UtilTest` and `UiLogicTest` (chat markdown, session groups, `ApiException.uiText`, `AppLocale`). Network and SSE tests live in `shared`.

## Conventions

- All user-facing text lives in string resources: Turkish by default (`res/values`), English in `values-en`. iOS gets the same keys through the generator, so add new strings to Android first and rerun the script.
- Plan limits (Free: 5 messages a day, 3 products, the 10 newest chats; Premium: 100, 8, unlimited) appear in the Android strings, the iOS paywall table, the web `lib/site.ts` and the backend `src/services/plan.ts`. Change them together.
- Keep the SSE field names in sync with the backend's `services/agent.ts`.
