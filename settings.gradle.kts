pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "kotlin-semver"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")