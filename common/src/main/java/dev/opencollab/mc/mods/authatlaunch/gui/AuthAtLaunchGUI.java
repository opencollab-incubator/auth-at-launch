package dev.opencollab.mc.mods.authatlaunch.gui;


import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.async.AuthAtLaunchExecutorService;
import dev.opencollab.mc.mods.authatlaunch.auth.CompletedMinecraftAuthentication;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigManager;
import dev.opencollab.mc.mods.authatlaunch.config.AuthAtLaunchConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Log4j2
public class AuthAtLaunchGUI {

    private final AuthAtLaunchConfigManager configManager;
    private final AuthAtLaunchExecutorService executorService;
    private final CompletableFuture<CompletedMinecraftAuthentication> authResult;

    public void guiBasedAuthentication() {
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(() -> {
            log.info("Inside SwingUtilities invoke");
            AuthAtLaunchFrame authFrame = new AuthAtLaunchFrame(configManager, executorService, authResult);
            authFrame.setVisible(true);


            authFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (!authResult.isDone()) {
                        authResult.complete(CompletedMinecraftAuthentication.builder()
                                        .accessToken("AuthAtLaunchIgnored")
                                        .userType(null)
                                .build());
                        authResult.completeExceptionally(new AuthAtLaunchException("Authentication refused"));
                    }
                }
            });

            authResult.whenComplete((result, throwable) -> {
                SwingUtilities.invokeLater(authFrame::dispose);
                try {
                    configManager.flush();
                } catch (AuthAtLaunchConfigurationException e) {
                    throw new RuntimeException("Couldn't flush config", e);
                }
            });
        });
    }


}
