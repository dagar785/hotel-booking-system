pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Hotel Booking System"
include(
    ":app",
    ":core:common", ":core:data", ":core:testing",
    ":payment-sdk",
    ":feature:search", ":feature:hoteldetail", ":feature:booking",
)
