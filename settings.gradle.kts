pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "revenuecat-shared"

include(":core")
include(":paywall-logic")
include(":paywall-compose")
include(":example")
