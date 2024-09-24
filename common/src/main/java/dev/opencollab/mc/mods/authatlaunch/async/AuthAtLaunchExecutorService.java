package dev.opencollab.mc.mods.authatlaunch.async;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.CompletedMinecraftAuthentication;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

@Getter
@Log4j2
public class AuthAtLaunchExecutorService implements AutoCloseable {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {

        private final ThreadGroup authAtLaunchThreadGroup = new ThreadGroup("Auth At Launch");

        private int threadInitNumber = 0;
        private synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(authAtLaunchThreadGroup, r, "aal-" + nextThreadNum());
            thread.setUncaughtExceptionHandler((t, e) -> {
                log.error("Uncaught exception in thread {}", t.getName(), e);
            });
            return thread;
        }
    });


    @Override
    public void close() throws Exception {
        executorService.shutdown();
        if(!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
            throw new AuthAtLaunchException("Couldn't clean up Auth At Launch");
        };
    }
}
