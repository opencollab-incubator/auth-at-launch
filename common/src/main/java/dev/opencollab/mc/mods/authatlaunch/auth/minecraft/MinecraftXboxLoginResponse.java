package dev.opencollab.mc.mods.authatlaunch.auth.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class MinecraftXboxLoginResponse {
    String username;
    List<String> roles;
    @SerializedName("access_token")
    String accessToken;
    @SerializedName("token_type")
    String tokenType;
    @SerializedName("expires_in")
    Integer expiresIn;
}
