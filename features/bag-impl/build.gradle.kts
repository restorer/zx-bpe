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
        commonMain.dependencies {
            api(projects.features.bagPublic)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.features.testkitPublic)
        }
    }
}
