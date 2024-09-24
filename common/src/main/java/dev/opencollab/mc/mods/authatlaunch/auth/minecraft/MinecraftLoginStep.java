package dev.opencollab.mc.mods.authatlaunch.auth.minecraft;

import com.google.common.collect.Iterables;
import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.auth.MSAFailureException;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class MinecraftLoginStep implements AuthenticationStep<MinecraftXboxLoginResponse> {

    private final HTTPIO authIO;
    private final String uhs;
    private final String xblToken;

    @Override
    public MinecraftXboxLoginResponse perform() throws AuthAtLaunchException {

        WebResponse<MinecraftXboxLoginResponse> minecraftXboxLoginResponseWebResponse = authIO.makeSimpleJsonPostRequest(KnownValues.MINECRAFT_SERVICES_XBL_LOGIN, MinecraftXboxLoginResponse.class, MinecraftXboxLoginRequest.builder()
                .identityToken("XBL3.0 x="
                        + uhs
                        + ";"
                        + xblToken
                )
                .build());

        if (!minecraftXboxLoginResponseWebResponse.isSuccessful()) {
            throw new MSAFailureException("Authentication with Minecraft failed, status code: " + minecraftXboxLoginResponseWebResponse.getStatusCode());
        }
        return minecraftXboxLoginResponseWebResponse.getResponse();
    }
}
