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
    ":features:app",
    ":features:bag-impl",
    ":features:bag-processor",
    ":features:bag-public",
    ":features:testkit-public",
)
