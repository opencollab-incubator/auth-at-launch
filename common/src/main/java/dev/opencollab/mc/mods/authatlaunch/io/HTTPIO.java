package dev.opencollab.mc.mods.authatlaunch.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import dev.opencollab.mc.mods.authatlaunch.auth.entra.TokenIssueErrors;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Log4j2
public class HTTPIO {

    private HttpClient client;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(TokenIssueErrors.class,
                    (JsonDeserializer<TokenIssueErrors>) (json, typeOfT, context)
                            -> {
                        try {
                            return TokenIssueErrors.fromAPI(json.getAsString());
                        } catch (AuthAtLaunchWebException e) {
                            throw new RuntimeException(e);
                        }
                    })
            .create();

    public HTTPIO() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
                .build();
    }

    public <R> WebResponse<R> makeAuthenticatedJsonGetRequest(String path, Class<R> responseType, String authHeader) throws AuthAtLaunchWebException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .header("Authorization", authHeader)
                .GET()
                .build();

        return performRequest(path, responseType, request);
    }

    public <S> WebResponse<JsonObject> makeAmbiguousJsonPostRequest(String path, S send) throws AuthAtLaunchWebException {
        return makeSimpleJsonPostRequest(path, JsonObject.class, send);
    }

    public <S, R> WebResponse<R> makeSimpleJsonPostRequest(String path, Class<R> responseType, S send) throws AuthAtLaunchWebException {
        var requestBody = HttpRequest.BodyPublishers.ofString(gson.toJson(send));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .header("Content-Type", "application/json")
                .POST(requestBody)
                .build();

        return performRequest(path, responseType, request);
    }

    public WebResponse<JsonObject> makeAmbiguousFormPostRequest(String path, FormURLEncodable send) throws AuthAtLaunchWebException {
        return makeSimpleFormPostRequest(path, JsonObject.class, send);
    }

    public <R> WebResponse<R> makeSimpleFormPostRequest(String path, Class<R> responseType, FormURLEncodable send) throws AuthAtLaunchWebException {
        var requestBody = HttpRequest.BodyPublishers.ofString(send.toFormURLEncoded());
        var request = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(requestBody)
                .build();

        return performRequest(path, responseType, request);
    }

    private <R> WebResponse<R> performRequest(String path, Class<R> responseType, HttpRequest request) throws AuthAtLaunchWebException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            log.debug("REQ to {} RES {} {}", path, response.statusCode(), body);
            R responseBody = gson.fromJson(body, responseType);
            return WebResponse.<R>builder()
                    .headers(response.headers().map())
                    .statusCode(response.statusCode())
                    .response(responseBody)
                    .build();
        } catch (IOException | InterruptedException e) {
            throw new AuthAtLaunchWebException("Failed to make request to " + path, e);
        }
    }


    public <T> T convertToType(JsonObject jsonObject, Class<T> clazz) {
        return gson.fromJson(jsonObject, clazz);
    }

}
