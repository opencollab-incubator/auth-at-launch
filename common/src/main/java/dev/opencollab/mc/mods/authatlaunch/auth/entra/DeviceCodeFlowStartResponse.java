package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class DeviceCodeFlowStartResponse {
    @SerializedName("device_code")
    String deviceCode;
    @SerializedName("user_code")
    String userCode;
    @SerializedName("verification_uri")
    String verificationUri;
    @SerializedName("expires_in")
    Integer expiresIn;
    Integer interval;
    String message;
}
