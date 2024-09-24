package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import com.google.gson.JsonObject;
import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.auth.MSAFailureException;
import dev.opencollab.mc.mods.authatlaunch.auth.MSASoftFailureException;
import dev.opencollab.mc.mods.authatlaunch.io.AuthAtLaunchWebException;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class RefreshTokenStep implements AuthenticationStep<SuccessfulTokenResponse> {

    private final HTTPIO authIO;

    private final String refreshToken;

    @Override
    public SuccessfulTokenResponse perform() throws AuthAtLaunchException {
        WebResponse<JsonObject> jsonObjectWebResponse = authIO.makeAmbiguousFormPostRequest(KnownValues.MSA_TOKEN, RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build());
        if (jsonObjectWebResponse.isSuccessful()) {
            return authIO.convertToType(jsonObjectWebResponse.getResponse(), SuccessfulTokenResponse.class);
        } else {
            if (jsonObjectWebResponse.getStatusCode() < 400 || jsonObjectWebResponse.getStatusCode() >= 500) {
                log.error("Failed response from Microsoft: {} {}", jsonObjectWebResponse.getStatusCode(), jsonObjectWebResponse.getResponse());
                throw new MSAFailureException("Unexpected failure from Microsoft Authentication services (they could be down). Status code: " + jsonObjectWebResponse.getStatusCode());
            }
            FailedTokenResponse failedTokenResponse = authIO.convertToType(jsonObjectWebResponse.getResponse(), FailedTokenResponse.class);
            log.debug("Microsoft response ({}): {}", jsonObjectWebResponse.getStatusCode(), failedTokenResponse);
            throw new MSASoftFailureException(failedTokenResponse.getErrorDescription());
        }
    }
}
