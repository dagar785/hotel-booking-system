plugins { `kotlin-dsl` }

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.snakeyaml)
    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        register("androidApplication") { id = "hbs.android.application"; implementationClass = "com.hbs.buildlogic.AndroidApplicationConventionPlugin" }
        register("androidLibrary") { id = "hbs.android.library"; implementationClass = "com.hbs.buildlogic.AndroidLibraryConventionPlugin" }
        register("androidCompose") { id = "hbs.android.compose"; implementationClass = "com.hbs.buildlogic.AndroidComposeConventionPlugin" }
        register("androidHilt") { id = "hbs.android.hilt"; implementationClass = "com.hbs.buildlogic.AndroidHiltConventionPlugin" }
        register("testGate") { id = "hbs.test-gate"; implementationClass = "com.hbs.buildlogic.gate.TestGatePlugin" }
    }
}
