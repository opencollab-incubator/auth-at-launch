pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal {
            name = "Gradle Plugins Central (pluginManagement)"
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.architectury.dev/") {
                    name = "Architectury (pluginManagement)"
                }
            }
            filter {
                includeGroupByRegex("dev\\.architectury.*")
                includeGroup("architectury-plugin")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net/") {
                    name = "FabricMC (pluginManagement)"
                }
            }
            filter {
                includeGroup("net.fabricmc")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.minecraftforge.net/") {
                    name = "Forge (pluginManagement)"
                }
            }
            filter {
                includeGroup("net.minecraftforge")
                includeModule("de.oceanlabs.mcp", "mcinjector")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://libraries.minecraft.net/") {
                    name = "Minecraft Libraries (pluginManagement)"
                }
            }
            filter {
                includeModule("com.mojang", "datafixerupper")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.parchmentmc.org/") {
                    name = "Parchment Data (pluginManagement)"
                }
            }
            filter {
                includeGroup("org.parchmentmc.data")
            }
        }
    }
}


plugins {
    id("com.gradle.develocity") version "3.17.6"
}

rootProject.name = "auth-at-launch"


include(":common")
include(":fabric-like")
include(":fabric")
include(":quilt")
include(":forge")

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}
