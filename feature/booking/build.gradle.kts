plugins {
    id("hbs.android.library")
    id("hbs.android.compose")   // Compose is used only for the summary card embedded in XML (interop)
    id("hbs.android.hilt")
}
android {
    namespace = "com.hbs.feature.booking"
    buildFeatures.viewBinding = true
}
dependencies {
    implementation(project(":core:data"))
    implementation(project(":payment-sdk"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    testImplementation(project(":core:testing"))
}
