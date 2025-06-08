// buildscript block for build-level dependencies and repositories
buildscript {
    // Define extra properties for versions, similar to ext block in Groovy
    extra.apply {
        set("minV", 23)
        set("targetV", 34) // Consider updating this to a more recent API level like 33 or 34 if appropriate for new library
        set("vCode", 156) // This is for the old 'reactor' app, might not be relevant for the new lib
        set("vName", "1.5.6") // Same as above
    }

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Use uri() for URLs
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // Consider updating AGP version
        classpath(kotlin("gradle-plugin", version = "2.0.0")) // Consider updating Kotlin plugin version
    }
}

// Task for cleaning the build directory
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

// Note: `allprojects {}` or `subprojects {}` blocks, if present in original Groovy,
// would also need conversion. They are not in the provided snippet.
// If plugins like `com.android.application` or `com.android.library` are applied in the root
// (which is not typical for `buildscript` dependencies but for project plugins),
// they would be in a `plugins {}` block at the top level, not inside `buildscript`.
// The provided snippet seems to be a traditional root build file focusing on buildscript classpath.
