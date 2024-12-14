plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// https://docs.w3cub.com/kotlin/docs/reference/js-project-setup.html
// kotlinUpgradeYarnLock instead of kotlinUpgradePackageLock!

kotlin {
    jvm()

    js(IR) {
        browser {
            commonWebpackConfig {
                this.devtool = null
            }

            testTask {
                useKarma {
                    useSafari()
                }
            }
        }

        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("uuid", libs.versions.npm.uuid.get()))
            }
        }
    }
}
