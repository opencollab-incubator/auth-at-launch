plugins {
    id("aalbuild.architectury-component")
}

architectury {
    common("quilt", "fabric", "forge")
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
