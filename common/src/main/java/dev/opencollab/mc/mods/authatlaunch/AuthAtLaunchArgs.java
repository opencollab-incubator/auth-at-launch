package dev.opencollab.mc.mods.authatlaunch;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

@UtilityClass
@Log4j2
public class AuthAtLaunchArgs {

    public String[] authAtLaunchIfRequired(String[] args) throws AuthAtLaunchException {

        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        OptionSpec<String> usernameOS = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> uuidOS = optionParser.accepts("uuid").withRequiredArg();
        OptionSpec<String> xuidOS = optionParser.accepts("xuid").withOptionalArg().defaultsTo("");
        OptionSpec<String> clientIdOS = optionParser.accepts("clientId").withOptionalArg().defaultsTo("");
        OptionSpec<String> accessTokenOS = optionParser.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> userPropertiesOS = optionParser.accepts("userType").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> userTypeOS = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> profilePropertiesOS = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> allOtherOptions = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(args);

        List<String> otherOptionsList = optionSet.valuesOf(allOtherOptions);

        if(otherOptionsList.contains("--noAuthAtLaunch")) {
            log.info("Auth at Launch disabled via CLI argument");
            return args;
        }

        String s = optionSet.valueOf(accessTokenOS);
        if (s != null && s.length() > 10) { // Catch dummy access tokens like "0" and "FabricMC"
            log.info("Access token present - nothing to do");
            return args;
        }

        AuthAtLaunch authAtLaunch = new AuthAtLaunch();
        String[] strings = authAtLaunch.authenticationArgs();
        return ArrayUtils.addAll(strings, otherOptionsList.toArray(new String[0]));
    }
}
