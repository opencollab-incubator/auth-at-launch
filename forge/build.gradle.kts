plugins {
    id("aalbuild.architectury-component")
    id("aalbuild.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        mixinConfig("authatlaunch.mixins.json")
    }
}

val common by configurations.creating
val shadowCommon by configurations.creating
val developmentForge: Configuration = configurations.getByName("developmentForge") {
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
    forge(libs.lexforge)
    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        exclude("fabric.mod.json")

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
