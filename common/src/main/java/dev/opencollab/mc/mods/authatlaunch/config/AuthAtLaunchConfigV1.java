package dev.opencollab.mc.mods.authatlaunch.config;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

@Data
@Builder
class AuthAtLaunchConfigV1 implements VersionedConfig {

    @Builder.Default
    public Instant createdAt = Instant.now();

    @Builder.Default
    public Instant updatedAt = Instant.now();

    @Builder.Default
    public Integer formatVersion = 1;

    @Builder.Default
    public List<AuthAtLaunchConfigAuthenticatedUser> authenticatedUsers = new ArrayList<>();

    public String autoLoginUser;
}
