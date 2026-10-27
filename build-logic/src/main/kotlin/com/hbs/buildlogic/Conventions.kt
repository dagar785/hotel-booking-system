package com.hbs.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.lib(name: String) = catalog.findLibrary(name).get()

internal fun Project.androidExtension(): CommonExtension<*, *, *, *, *, *> =
    extensions.findByType(LibraryExtension::class.java) ?: extensions.getByType(ApplicationExtension::class.java)

internal fun Project.configureAndroidCommon() {
    androidExtension().apply {
        compileSdk = 35
        defaultConfig.minSdk = 26
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        testOptions.unitTests.isReturnDefaultValues = true
        lint {
            abortOnError = false
            checkReleaseBuilds = false
        }
        // Jacoco XML per module feeds the changed-line coverage gate.
        buildTypes.getByName("debug").enableUnitTestCoverage = true
    }
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    dependencies {
        add("testImplementation", lib("junit"))
        add("testImplementation", lib("coroutines-test"))
        add("testImplementation", lib("turbine"))
    }
    // Test gate wiring: hooks get installed on any build; coverage report is gate input.
    tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(":installGitHooks") }
    // Modules without unit tests have no Jacoco data; they only matter to the gate once they gain src/test.
    if (file("src/test").exists()) {
        val reportTask = "$path:createDebugUnitTestCoverageReport"
        rootProject.tasks.named("testGateCoverage") { dependsOn(reportTask) }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        configureAndroidCommon()
    }
}

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        configureAndroidCommon()
        extensions.configure<ApplicationExtension> { defaultConfig.targetSdk = 35 }
    }
}

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        dependencies {
            add("implementation", lib("hilt-android"))
            add("ksp", lib("hilt-compiler"))
        }
    }
}

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        androidExtension().buildFeatures.compose = true
        dependencies {
            add("implementation", platform(lib("compose-bom")))
            listOf("compose-ui", "compose-foundation", "compose-material3", "compose-ui-tooling-preview", "lifecycle-runtime-compose")
                .forEach { add("implementation", lib(it)) }
            add("debugImplementation", lib("compose-ui-tooling"))
        }
    }
}
