package dev.opencollab.mc.mods.authatlaunch;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AuthAtLaunchStandaloneMain {

    @SneakyThrows
    public static void main(String[] args) {
        AuthAtLaunch authAtLaunch = new AuthAtLaunch();
        log.info("Let's authenticate!");
        String[] strings = authAtLaunch.authenticationArgs();
        log.info("Authenticated! {}", (Object) strings);
    }
}
