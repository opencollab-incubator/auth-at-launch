package dev.opencollab.mc.mods.authatlaunch.gui;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.*;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.RefreshTokenStep;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.StartDeviceCodeFlowStep;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigManager;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.DeviceCodeFlowStartResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.SuccessfulTokenResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XboxLiveAuthenticationRequest;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XboxLiveAuthenticationRequestProperties;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XboxLiveAuthenticationResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XSTSAuthorizationRequest;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XSTSAuthorizationRequestProperties;
import dev.opencollab.mc.mods.authatlaunch.auth.xbl.XSTSAuthorizationResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftXboxLoginRequest;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftXboxLoginResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.minecraft.MinecraftProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class ExistingMicrosoftAuthenticationProcess {

    private final AuthenticationDetails details;
    private final AuthAtLaunchConfigManager manager;
    private final AuthAtLaunchFrame gui;

    public void refreshAndAuthenticate() {
        // Implement Microsoft device code flow here
        try {

            HTTPIO authIO = new HTTPIO();

            if(details.getMcRefreshAccessTokenAfter().isAfter(Instant.now())) {
                gui.completeAuthentication(CompletedMinecraftAuthentication.builder()
                                .uuid(details.getMcUUID())
                                .xuid(details.getXuid())
                                .username(details.getMcUsername())
                                .accessToken(details.getMcAccessToken())
                        .build());
                return;
            }

            // Minecraft Access Token is deadlined...
            String accessToken = details.getMsAccessToken();
            String refreshToken = details.getMsRefreshToken();
            Instant accessTokenDeadline = details.getMsRefreshAccessTokenAfter();
            if(accessTokenDeadline.isBefore(Instant.now())) {
                gui.infoMessage("Refreshing MSA token...");
                accessTokenDeadline = Instant.now();
                SuccessfulTokenResponse response = new RefreshTokenStep(authIO, refreshToken).perform();
                accessTokenDeadline = accessTokenDeadline.plus(Integer.parseInt(response.getExpiresIn()) - 60, ChronoUnit.SECONDS);
                accessToken = response.getAccessToken();
                refreshToken = response.getRefreshToken();
            }

            gui.infoMessage("Refreshing Minecraft profile...");

            XBLMinecraftCombinedStep.Result xblMinecraftAuthResult = new XBLMinecraftCombinedStep(authIO, accessToken, gui::infoMessage).perform();
            log.info("Authentication success!");

            manager.successfulAuthentication(AuthenticationDetails.builder()
                    .msAccessToken(accessToken)
                    .msRefreshToken(refreshToken)
                    .msRefreshAccessTokenAfter(accessTokenDeadline)
                    .xuid(xblMinecraftAuthResult.xuid())
                    .mcUsername(xblMinecraftAuthResult.username())
                    .mcUUID(xblMinecraftAuthResult.uuid())
                    .mcAccessToken(xblMinecraftAuthResult.accessToken())
                    .mcRefreshAccessTokenAfter(xblMinecraftAuthResult.accessTokenDeadline())
                    .build());

            gui.completeAuthentication(CompletedMinecraftAuthentication.builder()
                    .uuid(xblMinecraftAuthResult.uuid())
                    .xuid(xblMinecraftAuthResult.xuid())
                    .username(xblMinecraftAuthResult.username())
                    .accessToken(xblMinecraftAuthResult.accessToken())
                    .build());


        } catch (MSASoftFailureException ex) {
            log.info("MSA Failing softly", ex);
            manager.hardFailAuthentication(details);
            SwingUtilities.invokeLater(() -> gui.onAuthenticationFailure(ex.getMessage()));
        } catch (MSAFailureException ex) {
            gui.completeExceptionally(ex);
        } catch (AuthAtLaunchException e) {
            gui.completeExceptionally(new MSAFailureException("Failure during MSA Authentication", e));
        } catch (Exception ex) {
            gui.completeExceptionally(new MSAFailureException("Internal Failure during MSA Authentication", ex));
        }
    }

}
