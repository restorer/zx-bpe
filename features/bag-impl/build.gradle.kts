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
            dependencies {
                implementation(projects.features.bagPublic)
            }
        }

        @Suppress("unused")
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(projects.features.testkitPublic)
            }
        }
    }
}
