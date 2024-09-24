package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.auth.MSAFailureException;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class XboxLiveAuthenticationStep implements AuthenticationStep<XboxLiveAuthenticationResponse> {

    private final HTTPIO authIO;
    private final String msAccessToken;

    @Override
    public XboxLiveAuthenticationResponse perform() throws AuthAtLaunchException {
        WebResponse<XboxLiveAuthenticationResponse> xboxLiveAuthenticationResponseWebResponse = authIO.makeSimpleJsonPostRequest(KnownValues.XBL_AUTHENTICATE, XboxLiveAuthenticationResponse.class, XboxLiveAuthenticationRequest.builder()
                .properties(XboxLiveAuthenticationRequestProperties.builder()
                        .rpsTicket("d=" + msAccessToken)
                        .build())
                .build());
        if (!xboxLiveAuthenticationResponseWebResponse.isSuccessful()) {
            throw new MSAFailureException("Authentication with Xbox Live failed, status code: " + xboxLiveAuthenticationResponseWebResponse.getStatusCode());
        }
        return xboxLiveAuthenticationResponseWebResponse.getResponse();
    }
}
