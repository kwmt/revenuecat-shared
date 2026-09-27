import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Maven Central（Central Portal）に公開する設定。
 *
 * 公開先・署名・POM は `gradle.properties` の `mavenCentralPublishing` / `signAllPublications` / `POM_*` で決める
 * （com.vanniktech.maven.publish がプロパティから読む）。プラグインの型を使わないのは、Gradle 8.13 の Kotlin DSL
 * （Kotlin 2.0）が 0.37.0 のクラスを読めないため。
 *
 * 認証と署名は Gradle のプロパティ（CI では `ORG_GRADLE_PROJECT_` を付けた環境変数）で渡す:
 * - `mavenCentralUsername` / `mavenCentralPassword`: Central Portal のユーザートークン
 * - `signingInMemoryKey` / `signingInMemoryKeyPassword`: ASCII 形式の GPG 秘密鍵とそのパスワード
 */
class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.vanniktech.maven.publish")
    }
}
