pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "revenuecat-shared"

include(":core")
include(":paywall-logic")
include(":paywall-compose")
