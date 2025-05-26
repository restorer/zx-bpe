plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.features.bagPublic)
}
