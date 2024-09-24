package dev.opencollab.mc.mods.authatlaunch.config;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
class AuthAtLaunchConfigAuthenticatedUser {
    String xuid;
    String username;
    String mcUUID;
    UUID refreshTokenSecretRef;
    UUID microsoftAccessTokenSecretRef;
    UUID minecraftAccessTokenSecretRef;
    Instant minecraftAccessTokenDeadline;
    Instant microsoftAccessTokenDeadline;
}
