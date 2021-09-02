plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")

    id("dagger.hilt.android.plugin")

    // id("com.google.gms.google-services")
}

android {
    compileSdk = Versions.targetSdk
    buildToolsVersion = Versions.buildToolsVersion

    defaultConfig {
        applicationId = "com.shiftstudio.workflowshenanigans"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0.0-alpha01"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            // We need to sign debug builds with a debug key to make firebase auth happy
            storeFile = rootProject.file("release/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isDefault = true
            // applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeVersion"] as String
    }
}

// hilt {
//     enableAggregatingTask = true
// }

dependencies {

    coreLibraryDesugaring(Dependencies.Android.coreLibraryDesugaring)

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.android)
    implementation(KotlinX.coroutines.playServices)
    implementation(KotlinX.serialization.json)

    implementation(Google.dagger.hilt.android)
    implementation(Dependencies.AndroidX.Hilt.navCompose)
    kapt(Google.dagger.hilt.compiler)

    implementation(Google.Android.material)

    implementation(AndroidX.activity.compose)
    implementation(AndroidX.Compose.ui)
    implementation(AndroidX.Compose.material)
    implementation(Dependencies.AndroidX.Compose.uiToolingPreview)
    debugImplementation(AndroidX.Compose.ui.tooling)

    implementation(AndroidX.Navigation.compose)

    implementation(Google.accompanist.insets)
    implementation(Google.accompanist.insets.ui)
    implementation(Google.accompanist.systemuicontroller)
    implementation(Google.accompanist.swiperefresh)

    implementation(COIL.compose)

    implementation(Dependencies.Square.Workflow.compose)
    implementation(Dependencies.Square.Workflow.composeTooling)

    implementation(AndroidX.lifecycle.runtimeKtx)
    implementation(AndroidX.lifecycle.viewModelKtx)
    implementation(AndroidX.lifecycle.viewModelCompose)
    implementation(AndroidX.lifecycle.viewModelSavedState)

    implementation(platform(Firebase.bom))
    implementation(Firebase.authenticationKtx)
    implementation(Dependencies.Firebase.uiAuth)

    debugImplementation(Square.leakCanary.android)

    testImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.ext.junitKtx)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(AndroidX.Compose.ui.testJunit4)
}
