import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Firebase (FCM price alerts) needs app/google-services.json from the Firebase console.
// Without it the plugin is skipped and the app runs with push notifications disabled.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

android {
    namespace = "com.neilb.synapcart"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.neilb.synapcart"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Public web pages (privacy policy, terms, support) linked from the app.
        buildConfigField("String", "WEB_URL", "\"https://synapcart.emreaydin.dev/\"")

        // Play Billing. OWNER: must match the Play Console subscription and its base plans.
        buildConfigField("String", "PREMIUM_PRODUCT_ID", "\"synapcart_premium\"")
        buildConfigField("String", "PREMIUM_PLAN_MONTHLY", "\"monthly\"")
        buildConfigField("String", "PREMIUM_PLAN_YEARLY", "\"yearly\"")
    }

    // Release signing: create `keystore.properties` in the project root (gitignored) with
    // storeFile, storePassword, keyAlias, keyPassword. Without it the release build is unsigned.
    val keystorePropsFile = rootProject.file("keystore.properties")
    val releaseSigning = if (keystorePropsFile.exists()) {
        val props = Properties().apply { keystorePropsFile.inputStream().use { load(it) } }
        signingConfigs.create("release") {
            storeFile = rootProject.file(props.getProperty("storeFile"))
            storePassword = props.getProperty("storePassword")
            keyAlias = props.getProperty("keyAlias")
            keyPassword = props.getProperty("keyPassword")
        }
    } else null

    buildTypes {
        debug {
            // Local backend through the emulator's host loopback. Override with -PdebugBaseUrl=...
            val debugBaseUrl = (project.findProperty("debugBaseUrl") as String?) ?: "http://10.0.2.2:8001/"
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://api.synapcart.emreaydin.dev/\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            releaseSigning?.let { signingConfig = it }
        }
        // Release-like build against the staging Worker (staging-api / staging web).
        create("staging") {
            initWith(getByName("release"))
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("release")
            buildConfigField("String", "BASE_URL", "\"https://staging-api.synapcart.emreaydin.dev/\"")
            buildConfigField("String", "WEB_URL", "\"https://staging.synapcart.emreaydin.dev/\"")
            releaseSigning?.let { signingConfig = it } ?: run { signingConfig = signingConfigs.getByName("debug") }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Shared KMP module: API client (Ktor), repositories, session storage, business rules
    implementation(project(":shared"))

    // Play Billing and Firebase Cloud Messaging
    implementation(libs.billing.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Image Loading - Coil
    implementation(libs.coil.compose)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    //Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt & Navigation Compose
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}