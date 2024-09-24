plugins {
    id("aalbuild.architectury-component")
}

architectury {
    common("fabric", "quilt")
}

dependencies {
    modImplementation(libs.fabric.loader)

    compileOnly(project(":common", configuration = "namedElements")) { isTransitive = false }
}
