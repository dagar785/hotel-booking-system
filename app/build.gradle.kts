plugins {
    id("hbs.android.application")
    id("hbs.android.compose")
    id("hbs.android.hilt")
}
android {
    namespace = "com.hbs.app"
    defaultConfig {
        applicationId = "com.hbs.app"
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":payment-sdk"))
    implementation(project(":feature:search"))
    implementation(project(":feature:hoteldetail"))
    implementation(project(":feature:booking"))
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    testImplementation(project(":core:testing"))
}
