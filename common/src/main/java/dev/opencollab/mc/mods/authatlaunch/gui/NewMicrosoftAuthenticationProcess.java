package dev.opencollab.mc.mods.authatlaunch.gui;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.*;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.DeviceCodeFlowStartResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.StartDeviceCodeFlowStep;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.SuccessfulTokenResponse;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.WaitForDeviceCodeTokenStep;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigManager;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Log4j2
public class NewMicrosoftAuthenticationProcess {


    private final AuthAtLaunchConfigManager manager;
    private final AuthAtLaunchFrame gui;


    protected void doMSA() {

        // Implement Microsoft device code flow here
        try {
            HTTPIO authIO = new HTTPIO();
            gui.infoMessage("Talking to Microsoft to start authentication...");
            DeviceCodeFlowStartResponse deviceCodeFlowStartResponse = new StartDeviceCodeFlowStep(authIO).perform();
            log.info("Go to {} and use code {} to authenticate", deviceCodeFlowStartResponse.getVerificationUri(), deviceCodeFlowStartResponse.getUserCode());
            gui.infoMessage("Waiting for Device Code authentication... (pop up dialog)");
            final MSAPopUpAuthFrame popUpAuthFrame = new MSAPopUpAuthFrame(gui, deviceCodeFlowStartResponse);
            Instant dontUseAccessTokenAfter = Instant.now();
            SuccessfulTokenResponse successfulTokenResponse = null; // We take our Point of time from just before the request
            try {
                SwingUtilities.invokeLater(() -> {
                    popUpAuthFrame.setVisible(true);
                });
                successfulTokenResponse = new WaitForDeviceCodeTokenStep(authIO, deviceCodeFlowStartResponse.getDeviceCode(), deviceCodeFlowStartResponse.getInterval()).perform();
            } finally {
                SwingUtilities.invokeLater(popUpAuthFrame::dispose);
            }
            // We don't want to use the access token in the final minute of its lifetime to account for clock drift on client devices
            dontUseAccessTokenAfter = dontUseAccessTokenAfter.plus(Integer.parseInt(successfulTokenResponse.getExpiresIn()) - 60, ChronoUnit.SECONDS);
            log.info("Successful token got!! {}", successfulTokenResponse);

            XBLMinecraftCombinedStep.Result xblMinecraftAuthResult = new XBLMinecraftCombinedStep(authIO, successfulTokenResponse.getAccessToken(), gui::infoMessage).perform();
            log.info("Authentication success!");

            manager.successfulAuthentication(AuthenticationDetails.builder()
                    .msAccessToken(successfulTokenResponse.getAccessToken())
                    .msRefreshToken(successfulTokenResponse.getRefreshToken())
                    .msRefreshAccessTokenAfter(dontUseAccessTokenAfter)
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
