package dev.opencollab.mc.mods.authatlaunch.io;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class WebResponse<T> {
    Map<String, List<String>> headers;
    Integer statusCode;
    T response;

    public boolean isSuccessful() {
        return statusCode != null && statusCode >= 200 && statusCode < 300;
    }
}
