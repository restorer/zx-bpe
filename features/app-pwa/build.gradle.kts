import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// https://docs.w3cub.com/kotlin/docs/reference/js-project-setup.html
// kotlinUpgradeYarnLock instead of kotlinUpgradePackageLock!

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                if ((rootProject.findProperty("bpe.production") as? String)?.toBoolean() == true) {
                    this.mode = KotlinWebpackConfig.Mode.PRODUCTION
                }

                this.devtool = null
            }
        }

        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(projects.features.bagImpl)
            implementation(projects.features.engineImpl)
            implementation(projects.features.uiPwaImpl)
        }
    }
}
