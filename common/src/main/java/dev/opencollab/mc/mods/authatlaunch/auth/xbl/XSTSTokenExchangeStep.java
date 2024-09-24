package dev.opencollab.mc.mods.authatlaunch.auth.xbl;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.auth.MSAFailureException;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class XSTSTokenExchangeStep implements AuthenticationStep<XSTSAuthorizationResponse> {

    private final HTTPIO authIO;
    private final String xblToken;

    @Override
    public XSTSAuthorizationResponse perform() throws AuthAtLaunchException {
        WebResponse<XSTSAuthorizationResponse> xstsAuthorizationResponseWebResponse = authIO.makeSimpleJsonPostRequest(KnownValues.XSTS_TOKEN, XSTSAuthorizationResponse.class, XSTSAuthorizationRequest
                .builder()
                .properties(XSTSAuthorizationRequestProperties.builder()
                        .userTokens(List.of(xblToken))
                        .build())
                .build());

        if (!xstsAuthorizationResponseWebResponse.isSuccessful()) {
            // TODO error handling
            throw new MSAFailureException("Token exchange with Xbox Live failed, status code: " + xstsAuthorizationResponseWebResponse.getStatusCode());
        }
        return xstsAuthorizationResponseWebResponse.getResponse();
    }
}
