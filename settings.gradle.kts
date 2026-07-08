pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = rootProject.projectDir.toURI().resolve("libs/")
        }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                val props = java.util.Properties().also { p ->
                    val f = java.io.File(settingsDir, "local.properties")
                    if (f.exists()) f.inputStream().use(p::load)
                }
                password = props.getProperty("MAPBOX_DOWNLOAD_TOKEN", "")
            }
        }
    }
}

rootProject.name = "Shopzen"
include(":app")
include(":domain")
include(":data")
include(":presentation")
//yousef
//yousef
//rashed
//rashed
//nour
//nour
//ziad
//ziad
