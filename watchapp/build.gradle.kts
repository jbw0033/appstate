plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.appstatewatch"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appstatewatch"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appstate)
    implementation(libs.androidx.transform)
    implementation(project(":navigation3-appstate"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3.windowsizeclass)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.guava)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.wear.compose.navigation3)
    implementation(libs.play.services.wearable)
    implementation(libs.protolayout)
    implementation(libs.protolayout.material3)
    implementation(libs.tiles)
    implementation(libs.tiles.tooling.preview)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.watchface.complications.data.source.ktx)
    implementation(libs.wear.tooling.preview)
}