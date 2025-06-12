import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
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
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                api(projects.features.enginePublic)

                implementation(libs.kotlinx.coroutines)
                implementation(projects.features.bagImpl)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.features.testkitPublic)
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
