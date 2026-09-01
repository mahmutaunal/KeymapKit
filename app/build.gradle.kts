plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val premiumTestMode = providers.gradleProperty("keymapkit.premium.test").getOrElse("false").also {
    require(it == "true" || it == "false") {
        "keymapkit.premium.test must be either true or false"
    }
}

// Firebase remains optional for local/open-source builds. Dropping the Firebase console's
// google-services.json into app/ activates Analytics and Remote Config for release builds.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

extensions.configure<com.android.build.api.dsl.ApplicationExtension>("android") {
    namespace = "com.alpware.keymapkit"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.alpware.keymapkit"
        minSdk = 28
        targetSdk = 37
        versionCode = 14
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["admobAppId"] = ""
        buildConfigField("String", "ADMOB_BANNER_ID", "\"\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"\"")
        buildConfigField("boolean", "PREMIUM_TEST_MODE", "false")
    }

    buildTypes {
        debug {
            // Google sample IDs protect the open-source/debug workflow from invalid traffic.
            buildConfigField("boolean", "PREMIUM_TEST_MODE", premiumTestMode)
        }
        release {
            manifestPlaceholders["admobAppId"] = ""
            buildConfigField("String", "ADMOB_BANNER_ID", "\"\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"\"")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.review)
    implementation(libs.play.update)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.billing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
