rootProject.name = "bpe"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(
    ":features:app-pwa",
    ":features:bag-impl",
    ":features:bag-processor",
    ":features:bag-public",
    ":features:engine-public",
    ":features:engine-impl",
    ":features:testkit-public",
    ":features:ui-pwa-impl",
)
