plugins {
    id("aalbuild.java-17")
    id("architectury-plugin")
}

base {
    archivesName = "auth-at-launch"
}

version = "1.0-SNAPSHOT"
group = "dev.opencollab.mc.mods"

java {
    withSourcesJar()
}
