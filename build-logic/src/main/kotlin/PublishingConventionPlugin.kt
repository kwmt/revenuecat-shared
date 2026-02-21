import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import java.net.URI

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("maven-publish")

            group = project.property("GROUP") as String
            version = project.property("VERSION_NAME") as String

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    repositories {
                        maven {
                            name = "GitHubPackages"
                            url = URI("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "kwmt/revenuecat-shared"}")
                            credentials {
                                username = System.getenv("GITHUB_ACTOR") ?: ""
                                password = System.getenv("GITHUB_TOKEN") ?: ""
                            }
                        }
                    }
                }
            }
        }
    }
}
