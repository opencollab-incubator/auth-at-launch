package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.FormURLEncodable;
import dev.opencollab.mc.mods.authatlaunch.io.FormUtilities;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DeviceCodeTokenRequest implements FormURLEncodable {
    @Builder.Default
    String grantType = "urn:ietf:params:oauth:grant-type:device_code";
    @Builder.Default
    String clientId = KnownValues.PUBLIC_APP_ID;
    String deviceCode;

    @Override
    public String toFormURLEncoded() {
        return FormUtilities.serializeToUrlEncoded(Map.of(
                "grant_type", grantType,
                "client_id", clientId,
                "device_code", deviceCode
        ));
    }
}
