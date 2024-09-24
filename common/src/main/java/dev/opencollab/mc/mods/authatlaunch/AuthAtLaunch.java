package dev.opencollab.mc.mods.authatlaunch;

import dev.opencollab.mc.mods.authatlaunch.auth.AuthAtLaunchAuthenticator;
import dev.opencollab.mc.mods.authatlaunch.auth.CompletedMinecraftAuthentication;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
public class AuthAtLaunch {


    public String[] authenticationArgs() throws AuthAtLaunchException {
        final CompletableFuture<CompletedMinecraftAuthentication> completededMinecraftAuthentication = new CompletableFuture<>();
        try (AuthAtLaunchAuthenticator service = new AuthAtLaunchAuthenticator(completededMinecraftAuthentication)) {
            try {
                service.authenticateAsync();
                long loopStart = System.currentTimeMillis();
                do {
                    try {
                        CompletedMinecraftAuthentication completedMinecraftAuthentication = completededMinecraftAuthentication.get(30, TimeUnit.SECONDS);
                        List<String> buildingArgs = new ArrayList<>();
                        if (StringUtils.isNotBlank(completedMinecraftAuthentication.getAccessToken())) {
                            buildingArgs.add("--accessToken");
                            buildingArgs.add(completedMinecraftAuthentication.getAccessToken());
                        }
                        if (StringUtils.isNotBlank(completedMinecraftAuthentication.getUsername())) {
                            buildingArgs.add("--username");
                            buildingArgs.add(completedMinecraftAuthentication.getUsername());
                        }
                        if (StringUtils.isNotBlank(completedMinecraftAuthentication.getXuid())) {
                            buildingArgs.add("--xuid");
                            buildingArgs.add(completedMinecraftAuthentication.getXuid());
                        }
                        if (StringUtils.isNotBlank(completedMinecraftAuthentication.getUserType())) {
                            buildingArgs.add("--userType");
                            buildingArgs.add(completedMinecraftAuthentication.getUserType());
                        }
                        if (StringUtils.isNotBlank(completedMinecraftAuthentication.getUuid())) {
                            buildingArgs.add("--uuid");
                            buildingArgs.add(completedMinecraftAuthentication.getUuid());
                        }
                        return buildingArgs.toArray(new String[0]);

                    } catch (TimeoutException ex) {
                        log.info("Still waiting for Auth at Launch... is there a dialog window you've not noticed?");
                    }
                } while (System.currentTimeMillis() - loopStart < TimeUnit.MINUTES.toMillis(10));
                throw new AuthAtLaunchException("Reached timeout to complete Minecraft Authentication... go faster!");
            } catch (AuthAtLaunchException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new AuthAtLaunchException("Failed to resolve Minecraft Authentication", ex);
            }
        } catch (AuthAtLaunchException ex) {
            completededMinecraftAuthentication.completeExceptionally(ex);
            throw ex;
        } catch (Exception ex) {
            completededMinecraftAuthentication.completeExceptionally(ex);
            throw new AuthAtLaunchException("Failed closing Auth at Launch service cleanly", ex);
        }
    }


}
