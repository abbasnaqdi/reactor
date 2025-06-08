plugins {
    id("com.android.application")
    kotlin("android")
    // Removed: id("kotlin-android-extensions") as it's deprecated
}

android {
    compileSdk = rootProject.extra["targetV"] as Int // Should be 34 from root project

    defaultConfig {
        applicationId = "com.abbasnaqdi.sample" // Update application ID to new package convention
        minSdk = rootProject.extra["minV"] as Int       // Should be 23 from root project
        targetSdk = rootProject.extra["targetV"] as Int // Should be 34 from root project
        // versionCode and versionName can be kept or updated as needed for the sample
        versionCode = rootProject.extra["vCode"] as Int
        versionName = rootProject.extra["vName"] as String
        resConfigs("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Align with AGP 8.x default, compatible with Java 21 toolchain
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17" // Align with Java 17/21
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Set by Compose BOM typically, but good to specify compatible version
        // For Kotlin 2.0.0, Compose Compiler 1.5.12+ is needed.
        // The BOM should handle this, but we can specify explicitly if needed.
        // For example, if using Compose BOM 2024.05.00, compiler is 1.5.13
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(project(":library")) // Uses the new DataStore library

    // Standard AndroidX libraries (update versions)
    implementation("androidx.core:core-ktx:1.13.1") // Latest stable as of mid-2024
    implementation("androidx.appcompat:appcompat:1.7.0") // Latest stable
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2") // For lifecycleScope

    // Jetpack Compose
    val composeBomVersion = "2024.05.00" // Latest stable BOM as of mid-2024
    implementation(platform(kotlin("bom", composeBomVersion))) // Import Compose BOM for Kotlin-aligned versions
    implementation(platform("androidx.compose:compose-bom:${composeBomVersion}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Material3
    implementation("androidx.activity:activity-compose:1.9.0") // For ComponentActivity.setContent

    // ConstraintLayout is not typically used with Compose, can be removed if not needed for XML layouts
    // implementation("androidx.constraintlayout:constraintlayout:2.1.3") // Old version
    // If you need ConstraintLayout for Compose:
    // implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Test dependencies (can be updated too)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:${composeBomVersion}"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
