plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

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
    }

    sourceSets {
        @Suppress("unused")
        val commonMain by getting {
        }
    }
}
