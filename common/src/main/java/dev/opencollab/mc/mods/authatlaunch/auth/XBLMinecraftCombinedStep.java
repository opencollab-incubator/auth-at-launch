package dev.opencollab.mc.mods.authatlaunch.auth;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftLoginStep;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftProfileResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftProfileStep;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftXboxLoginResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XSTSAuthorizationResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XSTSTokenExchangeStep;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XboxLiveAuthenticationResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XboxLiveAuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Log4j2
public class XBLMinecraftCombinedStep implements AuthenticationStep<XBLMinecraftCombinedStep.Result> {

    private final HTTPIO authIO;
    private final String msAccessToken;
    private final Consumer<String> infoUpdateCallback;

    @Override
    public Result perform() throws AuthAtLaunchException {
        XboxLiveAuthenticationResponse xboxLiveAuthenticationResponse = new XboxLiveAuthenticationStep(authIO, msAccessToken).perform();
        infoUpdateCallback.accept("XSTS Token exchange...");
        XSTSAuthorizationResponse xstsAuthorizationResponse = new XSTSTokenExchangeStep(authIO, xboxLiveAuthenticationResponse.getToken()).perform();
        infoUpdateCallback.accept("Authenticating with Minecraft...");
        Instant mcAccessTokenUseDeadline = Instant.now();
        MinecraftXboxLoginResponse minecraftXboxLoginResponse = new MinecraftLoginStep(authIO, Iterables.getOnlyElement(xstsAuthorizationResponse.getDisplayClaims().getXui()).getUhs(), xstsAuthorizationResponse.getToken()).perform();
        mcAccessTokenUseDeadline = mcAccessTokenUseDeadline.plus(minecraftXboxLoginResponse.getExpiresIn(), ChronoUnit.SECONDS).minus(2, ChronoUnit.HOURS);

        String xuid = null;
        try {
            String[] jwtParts = minecraftXboxLoginResponse.getAccessToken().split("\\.");
            if (jwtParts.length != 3) {
                throw new MSAFailureException("Invalid JWT token as Minecraft access token.");
            }

            byte[] decode = Base64.getUrlDecoder().decode(jwtParts[1]);

            JsonObject jsonObject = JsonParser.parseString(new String(decode, StandardCharsets.UTF_8)).getAsJsonObject();

            if (!jsonObject.has("xuid")) {
                throw new MSAFailureException("XUID not found in the access token");
            }

            xuid = jsonObject.get("xuid").getAsString();
        } catch (Exception e) {
            log.warn("Couldn't extract XUID from Minecraft Access Token (format has likely changed). Your game should still start fine", e);
        }
        log.info("XUID: {}", xuid);


        infoUpdateCallback.accept("Getting Minecraft profile info...");

        MinecraftProfileResponse minecraftProfileResponse = new MinecraftProfileStep(authIO, minecraftXboxLoginResponse.getAccessToken()).perform();

        return new Result(minecraftProfileResponse.getName(), minecraftProfileResponse.getId(), xuid, minecraftXboxLoginResponse.getAccessToken(), mcAccessTokenUseDeadline);
    }

    public record Result(String username, String uuid, String xuid, String accessToken, Instant accessTokenDeadline) {
    }
}
