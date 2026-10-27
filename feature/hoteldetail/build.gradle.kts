plugins {
    id("hbs.android.library")
    id("hbs.android.compose")
    id("hbs.android.hilt")
}
android { namespace = "com.hbs.feature.hoteldetail" }
dependencies {
    implementation(project(":core:data"))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(project(":core:testing"))
}
