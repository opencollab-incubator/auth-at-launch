package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.FormURLEncodable;
import dev.opencollab.mc.mods.authatlaunch.io.FormUtilities;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DeviceCodeFlowStartRequest implements FormURLEncodable {
    @Builder.Default
    String clientId = KnownValues.PUBLIC_APP_ID;
    @Builder.Default
    String scope = "XboxLive.signin offline_access";

    @Override
    public String toFormURLEncoded() {
        return FormUtilities.serializeToUrlEncoded(
                Map.of("client_id", getClientId(), "scope", getScope())
        );
    }
}
