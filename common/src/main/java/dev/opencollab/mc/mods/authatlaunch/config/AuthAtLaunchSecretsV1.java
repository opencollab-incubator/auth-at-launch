package dev.opencollab.mc.mods.authatlaunch.config;


import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
class AuthAtLaunchSecretsV1 implements VersionedConfig {

    @Builder.Default
    public String _comment = "DO NOT SHARE THIS FILE OR ANY CONTENTS TO ANY OTHER PERSON!";

    @Builder.Default
    public Instant createdAt = Instant.now();

    @Builder.Default
    public Integer formatVersion = 1;

    @Builder.Default
    public Instant updatedAt = Instant.now();

    @Builder.Default
    Map<UUID, String> secrets = new HashMap<>();
}
