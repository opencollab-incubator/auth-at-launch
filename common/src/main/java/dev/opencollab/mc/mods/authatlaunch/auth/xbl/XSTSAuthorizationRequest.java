package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XSTSAuthorizationRequest {
    @SerializedName("Properties")
    XSTSAuthorizationRequestProperties properties;
    @SerializedName("RelyingParty")
    @Builder.Default
    String relyingParty = "rp://api.minecraftservices.com/";
    @SerializedName("TokenType")
    @Builder.Default
    String tokenType = "JWT";
}
