plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                this.devtool = null
            }
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(projects.features.bagPublic)
            implementation(projects.features.bagImpl)
            implementation(projects.features.enginePublic)
            implementation(npm("uuid", libs.versions.npm.uuid.get()))
        }
    }
}
