# iOS release checklist (App Store)

Work through this list once before the first submission. Placeholders in code are marked `OWNER:`.

## 1. Apple Developer and the App ID

1. Join the Apple Developer Program, then note your **Team ID** (Membership page).
2. Create the App ID `com.neilb.synapcart` (Identifiers) with these capabilities:
   - **Push Notifications**;
   - **Associated Domains**;
   - **In-App Purchase** (enabled by default).
3. Put the Team ID in these places:
   - `iosApp/project.yml` → `DEVELOPMENT_TEAM`;
   - `synapcart_web/public/.well-known/apple-app-site-association` → replace `REPLACE_WITH_TEAM_ID`, then deploy the web;
   - GitHub secret `APPLE_TEAM_ID` (for CI uploads).

If you change the bundle id, update `iosApp/Configs/Base.xcconfig`, `iosApp/fastlane/Appfile`, the AASA file and `APPLE_BUNDLE_ID` in `synapcart_backend/wrangler.jsonc`.

## 2. App Store Connect

1. **My Apps → +:** create the app (iOS, bundle id above, SKU `synapcart`, primary language Turkish). Copy the numeric **Apple ID** into:
   - `APPLE_APP_ID` in `synapcart_backend/wrangler.jsonc` (both environments);
   - `appStoreUrl` / `appStoreId` in `synapcart_web/lib/site.ts` (this also turns on the Smart App Banner).
2. **Subscriptions:** create the group `synapcart_premium` with two auto-renewable products:
   - `synapcart_premium_monthly` (1 month);
   - `synapcart_premium_yearly` (1 year).
   - Optionally add an introductory offer (free trial); the paywall shows it automatically.
   - Add TR and EN display names and descriptions, prices, and a review screenshot of the paywall.
   - The ids must match `PREMIUM_PRODUCT_*` in `iosApp/Configs/Base.xcconfig` and `APPLE_PRODUCT_IDS` in the backend.
3. **App Information → App Store Server Notifications:** choose Version 2.
   - Production URL: `https://api.synapcart.emreaydin.dev/api/v1/billing/apple/notifications`
   - Sandbox URL: `https://staging-api.synapcart.emreaydin.dev/api/v1/billing/apple/notifications`
4. **Users and Access → Integrations → App Store Connect API:** create a key with the App Manager role, for fastlane. Store these as GitHub secrets:
   - `ASC_KEY_ID`;
   - `ASC_ISSUER_ID`;
   - `ASC_KEY_CONTENT` (the `.p8` file, base64-encoded).
5. Optional, for faster status refreshes: create an **In-App Purchase** key and set it on the backend with `wrangler secret put APPLE_ISSUER_ID / APPLE_KEY_ID / APPLE_PRIVATE_KEY`.

## 3. Firebase (price-alert push)

1. In the Firebase project used for Android, **add an iOS app** with the same bundle id.
2. Download `GoogleService-Info.plist` and put it in `iosApp/SynapCart/Resources/` (gitignored). For CI, store it base64-encoded as the secret `GOOGLE_SERVICE_INFO_PLIST`.
3. In Firebase, go to **Project settings → Cloud Messaging → Apple app configuration** and upload an **APNs Auth Key** (.p8, created under Keys in the developer portal).
4. The backend already sends FCM v1 messages with an APNs payload; nothing changes there.

## 4. Build and TestFlight

```bash
cd iosApp
bundle install
TEAM_ID=XXXXXXXXXX ASC_KEY_ID=... ASC_ISSUER_ID=... ASC_KEY_CONTENT=... bundle exec fastlane beta_staging   # staging build
TEAM_ID=XXXXXXXXXX ASC_KEY_ID=... ASC_ISSUER_ID=... ASC_KEY_CONTENT=... bundle exec fastlane beta           # production build
```

You can also run them from GitHub: **Actions → iOS → Run workflow** with lane `beta` or `beta_staging`.

Test the sandbox purchase flow on TestFlight against the **staging** build. Staging uses `APPLE_ENVIRONMENT=Sandbox`; production accepts only Production transactions.

## 5. App Store listing

- **Privacy Policy URL:** `https://synapcart.emreaydin.dev/privacy`
- **Support URL:** `https://synapcart.emreaydin.dev/support`
- **App Privacy:** match `PrivacyInfo.xcprivacy`. Declare email, name, user ID, user content (chat messages), product interaction (favorites), purchase history and device ID (push token), all for App Functionality, linked to the user, and with no tracking.
- **Account deletion:** the app deletes the account in-app (Profile → Delete my account), and web deletion is available at `/delete-account`.
- **Review notes:**
  - Give a demo account (create one on staging/production) and explain that the assistant searches Google Shopping through SerpAPI.
  - Say that Premium is an auto-renewable subscription and that restore is on the paywall.
- **Screenshots:** 6.9" and 6.5" iPhone. Use the Debug build with the StoreKit configuration.
- **Age rating:** 4+, with no user-generated content shared publicly.
- **Encryption:** `ITSAppUsesNonExemptEncryption = NO` is already in Info.plist; the app uses only HTTPS.
