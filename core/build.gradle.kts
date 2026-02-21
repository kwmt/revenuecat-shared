plugins {
    id("revenuecat.android.library")
    id("revenuecat.kmp.library")
    id("revenuecat.publishing")
}

kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "RevenueCatSharedCore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.revenuecat.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "io.github.kwmt.revenuecat.core"
}
