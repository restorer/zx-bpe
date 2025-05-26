// import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

// https://docs.w3cub.com/kotlin/docs/reference/js-project-setup.html
// kotlinUpgradeYarnLock instead of kotlinUpgradePackageLock!

kotlin {
    jvm()

    js(IR) {
        browser {
            commonWebpackConfig {
                // this.mode = KotlinWebpackConfig.Mode.PRODUCTION
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
        @Suppress("unused")
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(projects.features.bagPublic)
                implementation(projects.features.bagImpl)
            }
        }

        @Suppress("unused")
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(projects.features.testkitPublic)
            }
        }

        @Suppress("unused")
        val jsMain by getting {
            dependencies {
                implementation(npm("uuid", libs.versions.npm.uuid.get()))
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.features.bagProcessor)
    add("kspJvm", projects.features.bagProcessor)
    add("kspJs", projects.features.bagProcessor)
}
