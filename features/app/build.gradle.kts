import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(projects.features.bagPublic)
                implementation(projects.features.bagImpl)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.features.testkitPublic)
        }

        jsMain.dependencies {
            implementation(npm("uuid", libs.versions.npm.uuid.get()))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.features.bagProcessor)
}

tasks.withType<KotlinCompilationTask<*>>().all {
    if (name != "compileCommonMainKotlinMetadata") {
        dependsOn("compileCommonMainKotlinMetadata")
    }
}
