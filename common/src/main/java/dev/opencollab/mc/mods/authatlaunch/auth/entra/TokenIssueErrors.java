package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import dev.opencollab.mc.mods.authatlaunch.io.AuthAtLaunchWebException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TokenIssueErrors {
    AUTHORIZATION_PENDING("authorization_pending"),
    AUTHORIZATION_DECLINED("authorization_declined"),
    BAD_VERIFICATION_CODE("bad_verification_code"),
    EXPIRED_TOKEN("expired_token"),
    ;

    private final String id;

    public static TokenIssueErrors fromAPI(String apiCode) throws AuthAtLaunchWebException {
        return switch (apiCode) {
            case "authorization_pending" -> AUTHORIZATION_PENDING;
            case "authorization_declined" -> AUTHORIZATION_DECLINED;
            case "bad_verification_code" -> BAD_VERIFICATION_CODE;
            case "expired_token" -> EXPIRED_TOKEN;
            default -> throw new AuthAtLaunchWebException("Unknown Microsoft error: " + apiCode);
        };
    }
}
