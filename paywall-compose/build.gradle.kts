plugins {
    id("revenuecat.android.library")
    id("revenuecat.kmp.library")
    id("revenuecat.publishing")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "RevenueCatSharedPaywallCompose"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":paywall-logic"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}

android {
    namespace = "io.github.kwmt.revenuecat.paywall.ui"
}
