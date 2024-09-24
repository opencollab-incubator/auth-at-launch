package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class XboxLiveAuthenticationResponseDisplayClaims {
    @SerializedName("xui")
    List<XboxLiveAuthenticationResponseDisplayClaimsXui> xui;
}
