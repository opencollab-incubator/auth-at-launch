plugins {
    id("aalbuild.architectury-component")
    id("aalbuild.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}


val common by configurations.creating
val shadowCommon by configurations.creating
val developmentFabric: Configuration = configurations.getByName("developmentFabric") {
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
    modImplementation(libs.fabric.loader)

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionFabric")) { isTransitive = false }
    common(project(":fabric-like", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":fabric-like", configuration = "transformProductionFabric")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
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
