package dev.opencollab.mc.mods.authatlaunch.auth.minecraft;

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
public class MinecraftProfileStep implements AuthenticationStep<MinecraftProfileResponse> {

    private final HTTPIO authIO;
    private final String mcAccessToken;

    @Override
    public MinecraftProfileResponse perform() throws AuthAtLaunchException {

        WebResponse<MinecraftProfileResponse> minecraftProfileResponseWebResponse = authIO.makeAuthenticatedJsonGetRequest(KnownValues.MINECRAFT_PROFILE, MinecraftProfileResponse.class, "Bearer " + mcAccessToken);

        if (!minecraftProfileResponseWebResponse.isSuccessful()) {
            throw new MSAFailureException("Fetching Minecraft profile failed, status code: " + minecraftProfileResponseWebResponse.getStatusCode());
        }

        return minecraftProfileResponseWebResponse.getResponse();
    }
}
