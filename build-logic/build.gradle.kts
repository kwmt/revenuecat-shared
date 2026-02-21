plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "revenuecat.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "revenuecat.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("publishing") {
            id = "revenuecat.publishing"
            implementationClass = "PublishingConventionPlugin"
        }
    }
}
