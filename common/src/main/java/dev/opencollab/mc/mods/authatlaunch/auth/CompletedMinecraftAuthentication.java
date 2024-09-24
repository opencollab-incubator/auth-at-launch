package dev.opencollab.mc.mods.authatlaunch.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletedMinecraftAuthentication {
    String username;
    String accessToken;
    String uuid;
    String xuid;
    @Builder.Default
    String userType = "msa";
}
