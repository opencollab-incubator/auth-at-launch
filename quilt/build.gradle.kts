plugins {
    id("aalbuild.architectury-component")
    id("aalbuild.shadow")
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://maven.quiltmc.org/repository/release/") {
                name = "QuiltMC (:quilt)"
            }
        }
        filter {
            includeGroupByRegex("org\\.quiltmc.*")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.terraformersmc.com/") {
                name = "terraformersmc (:quilt)"
            }
        }
        filter {
            includeModule("com.terraformersmc", "modmenu")
        }
    }
}

architectury {
    platformSetupLoomIde()
    loader("quilt")
}


val common by configurations.creating
val shadowCommon by configurations.creating
val developmentQuilt: Configuration = configurations.getByName("developmentQuilt") {
    extendsFrom(configurations["common"])
}

configurations {
    compileClasspath {
        extendsFrom(configurations["common"])
    }
    runtimeClasspath {
        extendsFrom(configurations["common"])
    }
}

dependencies {
    modImplementation(libs.quilt.loader)
    modApi(libs.quilted.fabric.api)

    localRuntime(libs.quilt.loader.dependencies)
    modLocalRuntime("com.terraformersmc:modmenu:7.2.2")
    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionQuilt")) { isTransitive = false }
    common(project(":fabric-like", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":fabric-like", configuration = "transformProductionQuilt")) { isTransitive = false }
}


tasks {
    processResources {
        inputs.property("group", project.group)
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand("version" to project.version,
                "group" to project.group)
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        injectAccessWidener = true
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar
        dependsOn(commonSources)
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
    skip()
}
