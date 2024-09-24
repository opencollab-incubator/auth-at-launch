package dev.opencollab.mc.mods.authatlaunch.auth;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;

public interface AuthenticationStep<T> {

    T perform() throws AuthAtLaunchException;

}
