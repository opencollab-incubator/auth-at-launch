package dev.opencollab.mc.mods.authatlaunch.gui;

import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDropdownOption {
    private String displayName;
    private OptionAction type;
    private AuthenticationDetails account;

    public enum OptionAction {
        EXISTING_MSA,
        NEW_MSA,
        OFFLINE_AUTH
    }

    @Override
    public String toString() {
        return displayName;
    }

}
