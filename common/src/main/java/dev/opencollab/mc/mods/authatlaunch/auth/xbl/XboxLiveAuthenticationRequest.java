package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XboxLiveAuthenticationRequest {
    @SerializedName("Properties")
    XboxLiveAuthenticationRequestProperties properties;
    @SerializedName("RelyingParty")
    @Builder.Default
    String relyingParty = "http://auth.xboxlive.com";
    @SerializedName("TokenType")
    @Builder.Default
    String tokenType = "JWT";
}
