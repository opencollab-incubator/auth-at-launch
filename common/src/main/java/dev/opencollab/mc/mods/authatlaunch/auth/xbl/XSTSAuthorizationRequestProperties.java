package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class XSTSAuthorizationRequestProperties {
    @SerializedName("SandboxId")
    @Builder.Default
    String sandboxId = "RETAIL";
    @SerializedName("UserTokens")
    List<String> userTokens;
}
