package dev.opencollab.mc.mods.authatlaunch.auth;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.async.AuthAtLaunchExecutorService;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigManager;
import dev.opencollab.mc.mods.authatlaunch.gui.AuthAtLaunchGUI;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class AuthAtLaunchAuthenticator implements AutoCloseable {

    private final AuthAtLaunchExecutorService executorService = new AuthAtLaunchExecutorService();
    private final CompletableFuture<CompletedMinecraftAuthentication> authResult;
    private volatile AuthAtLaunchGUI gui;

    public void authenticateAsync() {
        executorService.getExecutorService().execute(() -> {
            try {
                if(gui != null) {
                    throw new AuthAtLaunchException("Can't start GUI twice");
                }
                AuthAtLaunchConfigManager configManager = new AuthAtLaunchConfigManager();
                gui = new AuthAtLaunchGUI(configManager, executorService, authResult);
                gui.guiBasedAuthentication();
            } catch (Throwable t) {
                authResult.completeExceptionally(t instanceof AuthAtLaunchException ? t : new AuthAtLaunchException("Unexpected exception occured within Auth at Launch", t));
            }

        });
    }


    @Override
    public void close() throws Exception {
        executorService.close();
    }
}
