package dev.opencollab.mc.mods.authatlaunch.auth.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinecraftXboxLoginRequest {
    @SerializedName("IdentityToken")
    String identityToken;
}
