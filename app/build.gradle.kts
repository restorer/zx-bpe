plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// https://docs.w3cub.com/kotlin/docs/reference/js-project-setup.html

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(npm("uuid", libs.versions.npm.uuid.get()))
            }
        }
    }
}
