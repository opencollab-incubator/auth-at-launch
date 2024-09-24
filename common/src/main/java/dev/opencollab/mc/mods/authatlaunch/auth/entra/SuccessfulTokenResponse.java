package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SuccessfulTokenResponse {
    @SerializedName("token_type")
    String tokenType;
    String scope;
    @SerializedName( "expires_in")
    String expiresIn;
    @SerializedName("access_token")
    String accessToken;
    @SerializedName("id_token")
    String idToken;
    @SerializedName("refresh_token")
    String refreshToken;
}
