package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import dev.opencollab.mc.mods.authatlaunch.io.FormURLEncodable;
import dev.opencollab.mc.mods.authatlaunch.io.FormUtilities;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RefreshTokenRequest implements FormURLEncodable {
    @Builder.Default
    String clientId = KnownValues.PUBLIC_APP_ID;
    @Builder.Default
    String scope = "XboxLive.signin offline_access";
    @Builder.Default
    String grantType = "refresh_token";
    String refreshToken;

    @Override
    public String toFormURLEncoded() {
        return FormUtilities.serializeToUrlEncoded(Map.of(
                "client_id", clientId,
                "grant_type", grantType,
                "scope", scope,
                "refresh_token", refreshToken
        ));
    }
}
