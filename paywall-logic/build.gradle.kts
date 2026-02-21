plugins {
    id("revenuecat.android.library")
    id("revenuecat.kmp.library")
    id("revenuecat.publishing")
}

kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "RevenueCatSharedPaywallLogic"
            isStatic = true
            export(project(":core"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "io.github.kwmt.revenuecat.paywall.logic"
}
