# SynapCart Mobil (iOS ve Android)

SynapCart'ın yapay zekâ destekli alışveriş asistanı uygulamaları. Proje bir **Kotlin Multiplatform** projesidir:

| Klasör | İçerik |
|---|---|
| `shared/` | Ortak katman (Kotlin Multiplatform). Ktor ile API istemcisi ve SSE ajan akışı, kotlinx.serialization modelleri, repository'ler, oturum deposu (DataStore), şifre ve fiyat kuralları. Android'e kütüphane olarak, iOS'a `Shared.framework` olarak girer. |
| `iosApp/` | **iOS uygulaması**: SwiftUI, StoreKit 2 ile Premium, Firebase Messaging ile fiyat alarmları, Universal Links. İlk yayın iOS'ta olacak. |
| `app/` | **Android uygulaması**: Jetpack Compose, Hilt, Play Billing, FCM. |

Backend adresleri:

| Ortam | Adres |
|---|---|
| Production | `https://api.synapcart.emreaydin.dev` |
| Staging | `https://staging-api.synapcart.emreaydin.dev` |
| Debug | `localhost:8001` (yerel Worker) |

Web adresi: `https://synapcart.emreaydin.dev`

## Özellikler

- AI asistan ile sohbet: canlı ajan durumu (SSE), ürün kartları, sohbet geçmişi.
- Favoriler ve fiyat alarmları (Premium); fiyat düşünce bildirim gelir.
- Yapay zekâ ile 2–4 ürünü karşılaştırma (Premium).
- Ücretsiz ve Premium planlar:
  - iOS'ta App Store abonelikleri (StoreKit 2);
  - Android'de Google Play Billing;
  - satın alımlar backend tarafından doğrulanır.
- Uygulama içinden hesap silme, şifre değiştirme ve şifre sıfırlama (sıfırlama e-posta bağlantısı uygulamada açılır).
- Türkçe (varsayılan) ve İngilizce.

## iOS

Gereksinimler: macOS ve Xcode 16+, JDK 17+ (Kotlin framework'ü Gradle derler), [XcodeGen](https://github.com/yonaskolb/XcodeGen).

```bash
brew install xcodegen
cd iosApp && xcodegen generate && open SynapCart.xcodeproj
```

- **Şemalar:**
  - `SynapCart` Debug yapılandırmasında yerel backend'e (`http://localhost:8001/`) bağlanır, Release yapılandırmasında production'a.
  - `SynapCart Staging` staging'e bağlanır.
  - Adresler `iosApp/Configs/*.xcconfig` dosyalarındadır.
- **Kotlin framework'ü:** derleme öncesi adım `./gradlew :shared:embedAndSignAppleFrameworkForXcode` komutunu çalıştırır, elle bir şey yapmana gerek yok.
- **Satın alma testi:** Debug şeması `Configuration.storekit` ile çalışır, yani Xcode içinde App Store Connect olmadan satın alma denenebilir. Bu işlemler Sandbox ortamındadır; staging backend bunları kabul eder.
- **Push:** `GoogleService-Info.plist` dosyasını `iosApp/SynapCart/Resources/` altına koy (gitignored). Dosya yoksa push kapalı kalır.
- **Kaynaklar:** metinler, ikon ve fontlar Android'den üretilir:

  ```bash
  python3 iosApp/scripts/generate_resources.py ../synapcart/design/logo_foreground.png
  ```

App Store'a gönderim adımları için: [docs/IOS_RELEASE.md](docs/IOS_RELEASE.md).

## Android

```bash
./gradlew :app:installDebug                      # emülatör -> yerel backend (10.0.2.2:8001)
./gradlew :app:assembleStaging                   # staging backend
./gradlew :app:assembleRelease                   # production (R8; keystore.properties varsa imzalı)
```

## Testler

```bash
./gradlew :shared:testAndroidHostTest            # ortak katman (JVM)
./gradlew :shared:iosSimulatorArm64Test          # ortak katman (iOS simülatörü, macOS)
./gradlew :app:testDebugUnitTest :app:lintDebug  # Android
cd iosApp && xcodebuild test -scheme SynapCart -destination 'platform=iOS Simulator,name=iPhone 16'
```

GitHub Actions:
- `.github/workflows/android.yml`: testler, lint, release build ve iOS klib derlemesi;
- `.github/workflows/ios.yml`: macOS'ta Xcode derlemesi ve testleri. Elle çalıştırıldığında fastlane ile TestFlight'a yükleme de yapar.
