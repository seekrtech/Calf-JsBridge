import org.gradle.api.publish.PublishingExtension

allprojects {
    group = "com.mohamedrejeb.calf"
    version = System.getenv("VERSION") ?: "0.6.1-jsbridge"
}

// GitHub Packages publishing configuration for private repository
allprojects {
    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "YourUsername/YourRepositoryName"}")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR") ?: System.getenv("GITHUB_USERNAME")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}
