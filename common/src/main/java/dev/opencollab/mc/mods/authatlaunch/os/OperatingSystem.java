package dev.opencollab.mc.mods.authatlaunch.os;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public enum OperatingSystem {

    LINUX,
    WINDOWS,
    MAC_OS,
    UNKNOWN;

}
