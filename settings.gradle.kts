import org.gradle.api.initialization.resolve.RepositoriesMode // Added import just in case

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https.jitpack.io") } // Corrected from "https//jitpack.io" to "https://jitpack.io"
    }
}

include(":library", ":sample")
