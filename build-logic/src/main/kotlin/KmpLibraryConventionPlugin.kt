import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")

            val swiftLibRoot = swiftLibRootOrNull()

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(17)

                androidTarget {
                    publishLibraryVariants("release")
                }

                iosX64().linkSwiftCompatibility(swiftLibRoot, "iphonesimulator")
                iosArm64().linkSwiftCompatibility(swiftLibRoot, "iphoneos")
                iosSimulatorArm64().linkSwiftCompatibility(swiftLibRoot, "iphonesimulator")
            }
        }
    }

    /**
     * purchases-kmp 3.x は purchases-ios を Swift のまま同梱していて、古い iOS 向けに作った Swift のコードは
     * Swift の互換ライブラリ（libswiftCompatibility56 など）を要る。ところが同梱の設定が探す場所は、RevenueCat の CI の
     * Xcode（`/Applications/Xcode-16.4.app/…`）に決め打ちされている。
     *
     * ★この Mac で選ばれている Xcode の場所を足さないと、Kotlin/Native が自分でリンクする iOS のテストの実行ファイルが
     *   `Undefined symbols … __swift_FORCE_LOAD_$_swiftCompatibility56` で落ちる。
     * ★公開する成果物には効かない（リンクの設定は klib に入らない）。利用する側のアプリは Xcode が最後にリンクするので、
     *   互換ライブラリは Xcode が自分で探す。
     * ★`DEVELOPER_DIR` を渡せば、その Xcode の場所になる（`xcrun` がそれに従う）。
     */
    private fun Project.swiftLibRootOrNull(): String? {
        if (!System.getProperty("os.name").startsWith("Mac")) return null
        val swiftc = runCatching {
            providers.exec { commandLine("xcrun", "--find", "swiftc") }
                .standardOutput.asText.get().trim()
        }.getOrNull() ?: return null
        return File(swiftc).parentFile?.parentFile?.resolve("lib/swift")?.takeIf { it.isDirectory }?.path
    }

    private fun KotlinNativeTarget.linkSwiftCompatibility(swiftLibRoot: String?, platform: String) {
        if (swiftLibRoot == null) return
        binaries.all { linkerOpts("-L$swiftLibRoot/$platform") }
    }
}
