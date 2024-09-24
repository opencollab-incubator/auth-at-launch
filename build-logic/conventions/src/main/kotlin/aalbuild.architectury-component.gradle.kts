plugins {
    id("dev.architectury.loom")
    id("internal.aalbuild.architectury-all")
}

var minecraftVersion = versionCatalogs.named("libs").findVersion("minecraft").get().requiredVersion;

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    // The following line declares the mojmap mappings, you may use other mappings as well
    mappings(loom.officialMojangMappings())
    // The following line declares the yarn mappings you may select this one as well.
    // mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
}

loom {
    //silentMojangMappingsLicense()
}
