plugins {
    id("com.android.library")
    kotlin("android") // Replaces id 'kotlin-android'
    id("maven-publish")
}

android {
    // Assuming targetV, minV, vName are defined in root project's build.gradle or gradle.properties
    // and are accessible here. If not, these might need to be hardcoded or loaded differently.
    compileSdk = rootProject.extra["targetV"] as Int // Example access if defined in rootProject.extra

    defaultConfig {
        minSdk = rootProject.extra["minV"] as Int
        targetSdk = rootProject.extra["targetV"] as Int
        resConfigs.add("en") // Changed from resConfig "en"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Changed from minifyEnabled false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Changed from = 11
        targetCompatibility = JavaVersion.VERSION_11 // Changed from = 11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // The publishing block needs to be correctly translated.
    // Ensure `MavenPublication` and `components.release` are correctly referenced.
    // This might require type casting or specific Kotlin DSL for publishing.
    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("release") { // Use create<MavenPublication>
                    from(components["release"]) // Access components by string key
                    groupId = "com.github.aaaamirabbas"
                    artifactId = "reactor" // This is the old library's artifactId. Might need update for the new lib.
                    version = rootProject.extra["vName"] as String // Example access
                }
            }
        }
    }
}

dependencies {
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-proto:1.1.1")
    // Security for encryption
    implementation("androidx.security:security-crypto:1.0.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.11")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("io.mockk:mockk-android:1.13.11")
}
