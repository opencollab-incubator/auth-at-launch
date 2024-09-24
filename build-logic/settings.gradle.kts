pluginManagement {
    repositories {
        gradlePluginPortal {
            name = "Gradle Plugins Central (build-logic pluginManagement)"
        }
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.6"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":conventions")

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}