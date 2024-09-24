plugins {
    id ("internal.aalbuild.architectury-all")
}

var minecraftVersion = versionCatalogs.named("libs").findVersion("minecraft").get().requiredVersion;

architectury {
    minecraft = minecraftVersion
}
