package dev.opencollab.mc.mods.authatlaunch.auth;

import com.google.common.annotations.Beta;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder(toBuilder = true)
@Value
public class AuthenticationDetails {
    String xuid;
    String mcUUID;
    String mcUsername;
    String msAccessToken;
    String mcAccessToken;
    Instant mcRefreshAccessTokenAfter;
    String msRefreshToken;
    Instant msRefreshAccessTokenAfter;
}
