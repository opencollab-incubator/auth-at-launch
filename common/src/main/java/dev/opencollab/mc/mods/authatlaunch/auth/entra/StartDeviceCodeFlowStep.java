package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import dev.opencollab.mc.mods.authatlaunch.AuthAtLaunchException;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationStep;
import dev.opencollab.mc.mods.authatlaunch.io.HTTPIO;
import dev.opencollab.mc.mods.authatlaunch.io.KnownValues;
import dev.opencollab.mc.mods.authatlaunch.io.WebResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class StartDeviceCodeFlowStep implements AuthenticationStep<DeviceCodeFlowStartResponse> {

    private final HTTPIO authIO;


    @Override
    public DeviceCodeFlowStartResponse perform() throws AuthAtLaunchException {
        WebResponse<DeviceCodeFlowStartResponse> deviceAuthorizationResponseWebResponse = authIO.makeSimpleFormPostRequest(KnownValues.MSA_DEVICECODE, DeviceCodeFlowStartResponse.class, DeviceCodeFlowStartRequest.builder().build());
        if (!deviceAuthorizationResponseWebResponse.isSuccessful()) {
            throw new AuthAtLaunchException("Could not start device code flow. Status response from Microsoft was: " + deviceAuthorizationResponseWebResponse.getStatusCode());
        }
        return deviceAuthorizationResponseWebResponse.getResponse();
    }
}
