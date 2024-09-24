package dev.opencollab.mc.mods.authatlaunch.config;

import java.time.Instant;

public interface VersionedConfig {

    Integer getFormatVersion();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    void setUpdatedAt(Instant updatedAt);

}
