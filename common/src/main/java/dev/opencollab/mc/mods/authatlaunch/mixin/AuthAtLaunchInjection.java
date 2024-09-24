package dev.opencollab.mc.mods.authatlaunch.mixin;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchArgs;
import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
@Log4j2(topic = "AuthAtLaunchInjection")
public class AuthAtLaunchInjection {

    @ModifyVariable(at = @At("LOAD"), method = "main([Ljava/lang/String;)V", argsOnly = true, remap = false)
    private static String[] modifyArgs(String[] initialArgs) throws AuthAtLaunchException  {
        log.debug("Intercepting main method args");
        log.trace("initial args {}", (Object) initialArgs);
        String[] finalArgs = AuthAtLaunchArgs.authAtLaunchIfRequired(initialArgs);
        log.trace("final args {}", (Object) finalArgs);
        return finalArgs;
    }
}