package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class XSTSAuthorizationResponse {
    @SerializedName("IssueInstant")
    String issueInstant;
    @SerializedName("NotAfter")
    String notAfter;
    @SerializedName("Token")
    String token;
    @SerializedName("DisplayClaims")
    XboxLiveAuthenticationResponseDisplayClaims displayClaims;
}
