package dev.opencollab.mc.mods.authatlaunch.os;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Locale;

@Log4j2
@UtilityClass
public class OperatingSystemUtils {

    @Getter
    private final OperatingSystem operatingSystem = resolveOperatingSystem();

    @Getter
    private final Path globalConfigLocation = resolveDefaultGlobalConfigLocation();

    private OperatingSystem resolveOperatingSystem() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (string.contains("mac")) {
            return OperatingSystem.MAC_OS;
        } else if (string.contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return string.contains("unix") ? OperatingSystem.LINUX : OperatingSystem.UNKNOWN;
        }
    }

    private Path resolveDefaultGlobalConfigLocation() {
        Path defaultGlobalConfigLocation = Path.of(System.getProperty("user.home"), ".config", "open-collaboration", "auth-at-launch");
        OperatingSystem currentOS = getOperatingSystem();
        switch (currentOS) {
            case WINDOWS:
                defaultGlobalConfigLocation = Path.of(System.getenv("LOCALAPPDATA"), "Open Collaboration", "Auth at Launch");
                break;
            case LINUX:
                String xdgConfigDir = System.getProperty("XDG_CONFIG_DIR");
                if (StringUtils.isNotBlank(xdgConfigDir)) {
                    defaultGlobalConfigLocation = Path.of(xdgConfigDir, "open-collaboration", "auth-at-launch");
                }
                break;
            case MAC_OS:
                defaultGlobalConfigLocation = Path.of(System.getProperty("user.home"), "Library", "Application Support", "Open Collaboration", "Auth at Launch");
                break;
            default:
                defaultGlobalConfigLocation = Path.of(System.getProperty("user.home"), ".config", "open-collaboration", "auth-at-launch");
        }
        return defaultGlobalConfigLocation;
    }

}
