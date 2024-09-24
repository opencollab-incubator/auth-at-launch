package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;

public class XSTSAuthorizationResponseError {
    @SerializedName("Identity")
    String identity;
    @SerializedName("XErr")
    Integer xErr;
    @SerializedName("Message")
    String message;
    @SerializedName("Redirect")
    String redirect;
}
