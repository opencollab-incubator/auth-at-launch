package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class XboxLiveAuthenticationResponseDisplayClaimsXui {
    @SerializedName("uhs")
    String uhs;
}
