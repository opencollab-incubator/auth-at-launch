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
public class WaitForDeviceCodeTokenStep implements AuthenticationStep<SuccessfulTokenResponse> {

    private final HTTPIO authIO;
    private final String deviceCode;
    private final Integer interval;

    @Override
    public SuccessfulTokenResponse perform() throws AuthAtLaunchException {
        while(true) {
            WebResponse<JsonObject> jsonObjectWebResponse = authIO.makeAmbiguousFormPostRequest(KnownValues.MSA_TOKEN, DeviceCodeTokenRequest.builder()
                    .deviceCode(deviceCode)
                    .build());
            if (jsonObjectWebResponse.isSuccessful()) {
                return authIO.convertToType(jsonObjectWebResponse.getResponse(), SuccessfulTokenResponse.class);
            } else {
                if (jsonObjectWebResponse.getStatusCode() < 400 || jsonObjectWebResponse.getStatusCode() >= 500) {
                    log.error("Failed response from Microsoft: {} {}", jsonObjectWebResponse.getStatusCode(), jsonObjectWebResponse.getResponse());
                    throw new AuthAtLaunchWebException("Unexpected failure from Microsoft Authentication services (they could be down). Status code: " + jsonObjectWebResponse.getStatusCode());
                }
                FailedTokenResponse failedTokenResponse = authIO.convertToType(jsonObjectWebResponse.getResponse(), FailedTokenResponse.class);
                log.debug("Microsoft response ({}): {}", jsonObjectWebResponse.getStatusCode(), failedTokenResponse);
                switch (failedTokenResponse.getError()) {
                    case AUTHORIZATION_PENDING -> {
                        log.info("User hasn't yet authorized our app... Will poll again in {} seconds...", interval);
                        try {
                            Thread.sleep(interval * 1000L);
                        } catch (InterruptedException e) {
                            throw new MSAFailureException("Interrupted whilst waiting for poll", e);
                        }
                    }
                    case EXPIRED_TOKEN -> throw new MSASoftFailureException("Flow timed out, try again");
                    case AUTHORIZATION_DECLINED ->
                            throw new MSASoftFailureException("You declined authorization");
                    default -> throw new MSAFailureException(failedTokenResponse.getErrorDescription());
                }
            }
        }
    }
}
