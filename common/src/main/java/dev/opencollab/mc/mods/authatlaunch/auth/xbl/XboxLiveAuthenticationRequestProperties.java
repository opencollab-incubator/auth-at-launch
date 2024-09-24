package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XboxLiveAuthenticationRequestProperties {
    @SerializedName("AuthMethod")
    @Builder.Default
    String authMethod = "RPS";
    @SerializedName("SiteName")
    @Builder.Default
    String siteName = "user.auth.xboxlive.com";
    @SerializedName("RpsTicket")
    String rpsTicket;
}
