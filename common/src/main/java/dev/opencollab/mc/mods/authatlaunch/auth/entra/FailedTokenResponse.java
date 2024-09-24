package dev.opencollab.mc.mods.authatlaunch.auth.entra;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FailedTokenResponse {
    TokenIssueErrors error;
    @SerializedName("error_description")
    String errorDescription;
    @SerializedName("error_codes")
    List<String> errorCodes;
    String timestamp;
    @SerializedName("trace_id")
    UUID traceId;
    @SerializedName("correlation_id")
    UUID correlationId;
    @SerializedName("error_uri")
    String errorUri;
}
