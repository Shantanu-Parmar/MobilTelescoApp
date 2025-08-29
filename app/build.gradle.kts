plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.example.mobiltelesco"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.mobiltelesco"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.22.1"
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        // Removed viewBinding = true, as we're using Compose and it's not needed
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"  // Confirmed as compatible; for Kotlin 2.0+, it's integrated, but specify if using older Kotlin
    }

    namespace = "com.example.mobiltelesco"
}


dependencies {
// Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
// Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
// AppCompat and Material (if needed for hybrid; remove if pure Compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
// ConstraintLayout (if needed; remove if not using XML layouts)
    implementation(libs.androidx.constraintlayout)
    implementation("com.microsoft.onnxruntime:onnxruntime-mobile:1.18.0")
// CameraX - Updated to latest stable as of 2025 (1.5.0)
//    implementation("androidx.camera:camera-camera2:1.5.0")
//    implementation("androidx.camera:camera-lifecycle:1.5.0")
//    implementation("androidx.camera:camera-view:1.5.0")
    implementation ("androidx.camera:camera-core:1.5.0-beta02")
    implementation ("androidx.camera:camera-camera2:1.5.0-beta02")
    implementation ("androidx.camera:camera-lifecycle:1.5.0-beta02")
    implementation ("androidx.camera:camera-viewfinder:1.4.0-alpha07")
    implementation ("androidx.camera:camera-viewfinder-compose:1.0.0-alpha02")
    implementation ("androidx.compose.material:material")
    implementation(libs.onnxruntime.mobile)
// Tests
    implementation("androidx.camera:camera-view:1.5.0-beta02")
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
// Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("org.tensorflow:tensorflow-lite:2.8.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.0")
    implementation ("androidx.compose:compose-bom:2023.10.01")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.ui:ui-tooling")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")
    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("androidx.compose.animation:animation:1.6.7")
}