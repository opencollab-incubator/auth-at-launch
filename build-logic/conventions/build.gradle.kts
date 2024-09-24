plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    exclusiveContent {
        forRepository {
            maven("https://maven.architectury.dev/") {
                name = "Architectury (build-logic)"
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
                name = "FabricMC (build-logic)"
            }
        }
        filter {
            includeGroup("net.fabricmc")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.minecraftforge.net/") {
                name = "Forge (build-logic)"
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
                name = "Minecraft Libraries (build-logic)"
            }
        }
        filter {
            includeModule("com.mojang", "datafixerupper")
        }
    }
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.architectury.plugin)
    implementation(libs.architectury.loom)
}
