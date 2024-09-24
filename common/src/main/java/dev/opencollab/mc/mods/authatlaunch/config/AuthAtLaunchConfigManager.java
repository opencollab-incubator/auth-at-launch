package dev.opencollab.mc.mods.authatlaunch.config;

import com.google.gson.*;
import dev.opencollab.mc.mods.authatlaunch.auth.AuthenticationDetails;
import dev.opencollab.mc.mods.authatlaunch.os.OperatingSystemUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

@Log4j2
public class AuthAtLaunchConfigManager {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class,
                    (JsonSerializer<Instant>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Instant.class,
                    (JsonDeserializer<Instant>) (json, typeOfT, context)
                            -> Instant.parse(json.getAsString()))
            .create();

    private final File configurationDirectory;
    private final ConfigHolder<AuthAtLaunchConfigV1> config;
    private final ConfigHolder<AuthAtLaunchSecretsV1> secrets;

    public AuthAtLaunchConfigManager() throws AuthAtLaunchConfigurationException {
        Path configLocPath = OperatingSystemUtils.getGlobalConfigLocation();
        configurationDirectory = configLocPath.toFile();
        File configFile = configLocPath.resolve("config.v1.json").toFile();
        File secrets = configLocPath.resolve("secrets.v1.json").toFile();
        this.config = loadConfig("config", AuthAtLaunchConfigV1.class, configFile, () -> AuthAtLaunchConfigV1.builder().build());
        this.secrets = loadConfig("secrets", AuthAtLaunchSecretsV1.class, secrets, () -> AuthAtLaunchSecretsV1.builder().build());
    }

    private <T extends VersionedConfig> ConfigHolder<T> loadConfig(String type, Class<T> clazz, File path, Supplier<T> defaultSupplier) throws AuthAtLaunchConfigurationException {
        if (path.exists()) {
            try (FileReader reader = new FileReader(path)) {
                T t = gson.fromJson(reader, clazz);
                if (t == null) {
                    log.warn("Config JSON gave me null... will reset to default");
                    return new ConfigHolder<T>(path, defaultSupplier.get(), true);
                }
                return new ConfigHolder<T>(path, t, false);
            } catch (JsonIOException | IOException e) {
                throw new AuthAtLaunchConfigurationException("Failed to read " + type + " file at " + path.getPath(), e);
            } catch (JsonSyntaxException e) {
                throw new AuthAtLaunchConfigurationException("Invalid config " + type + " file at " + path.getPath(), e);
            }
        } else {
            return new ConfigHolder<T>(path, defaultSupplier.get(), true);
        }
    }

    public void flush() throws AuthAtLaunchConfigurationException {
        if (!isDirty()) {
            return;
        }
        if (!configurationDirectory.exists()) {
            if (!configurationDirectory.mkdirs()) {
                throw new AuthAtLaunchConfigurationException("Failed to create configuration directory at " + configurationDirectory.getPath());
            }
        }
        config.flush();
        secrets.flush();
    }

    public Optional<String> autoLoginUser() {
        return Optional.ofNullable(config.data.getAutoLoginUser());
    }

    public List<AuthenticationDetails> availableUsers() {
        List<AuthAtLaunchConfigAuthenticatedUser> authenticatedUsers = config.data.getAuthenticatedUsers();
        if (authenticatedUsers == null || authenticatedUsers.isEmpty()) {
            return Collections.emptyList();
        }
        // Most of the time the data set will be valid
        List<AuthenticationDetails> validAuthenticationDetails = new ArrayList<>(authenticatedUsers.size());
        Iterator<AuthAtLaunchConfigAuthenticatedUser> iterator = authenticatedUsers.iterator();
        while (iterator.hasNext()) {
            AuthAtLaunchConfigAuthenticatedUser authenticatedUser = iterator.next();
            String resolvedMSAccessToken = getSecret(authenticatedUser.getMicrosoftAccessTokenSecretRef());
            String resolvedMCAccessToken = getSecret(authenticatedUser.getMinecraftAccessTokenSecretRef());
            String resolvedRefreshToken = getSecret(authenticatedUser.getRefreshTokenSecretRef());
            if (StringUtils.isBlank(resolvedMSAccessToken) || StringUtils.isBlank(resolvedRefreshToken) || StringUtils.isBlank(resolvedMCAccessToken)) {
                log.warn("Lost secrets within {} account - removing account", authenticatedUser.getUsername());
                iterator.remove();
                removeAuthenticatedUser(authenticatedUser);
                markConfigModified();
                continue;
            }
            validAuthenticationDetails.add(AuthenticationDetails.builder()
                    .xuid(authenticatedUser.getXuid())
                    .mcUsername(authenticatedUser.getUsername())
                    .mcUUID(authenticatedUser.getMcUUID())
                    .msRefreshToken(resolvedRefreshToken)
                    .msAccessToken(resolvedMSAccessToken)
                    .mcAccessToken(resolvedMCAccessToken)
                            .msRefreshAccessTokenAfter(authenticatedUser.getMicrosoftAccessTokenDeadline())
                            .mcRefreshAccessTokenAfter(authenticatedUser.getMinecraftAccessTokenDeadline())
                    .build());
        }
        return Collections.unmodifiableList(validAuthenticationDetails);
    }

    public void successfulAuthentication(AuthenticationDetails completedMinecraftAuthentication) {
        // Ensure collection is initialised
        if (config.data.getAuthenticatedUsers() == null) {
            config.data.setAuthenticatedUsers(new ArrayList<>());
        }
        removeAllAuthenticationsWithUUID(completedMinecraftAuthentication.getMcUUID());
        config.data.getAuthenticatedUsers().add(AuthAtLaunchConfigAuthenticatedUser.builder()
                    .xuid(completedMinecraftAuthentication.getXuid())
                    .mcUUID(completedMinecraftAuthentication.getMcUUID())
                    .username(completedMinecraftAuthentication.getMcUsername())
                    .microsoftAccessTokenSecretRef(addSecret(completedMinecraftAuthentication.getMsAccessToken()))
                    .microsoftAccessTokenDeadline(completedMinecraftAuthentication.getMsRefreshAccessTokenAfter())
                    .refreshTokenSecretRef(addSecret(completedMinecraftAuthentication.getMsRefreshToken()))
                    .minecraftAccessTokenSecretRef(addSecret(completedMinecraftAuthentication.getMcAccessToken()))
                    .minecraftAccessTokenDeadline(completedMinecraftAuthentication.getMcRefreshAccessTokenAfter())
                .build());
        config.data.setAutoLoginUser(completedMinecraftAuthentication.getMcUUID());
        markConfigModified();
    }

    private void removeAllAuthenticationsWithUUID(String uuid) {
        Iterator<AuthAtLaunchConfigAuthenticatedUser> iterator = config.data.authenticatedUsers.iterator();
        // Clear out any other accounts which have the same UUID (we use MC UUID to detect duplicates as it's 1-1 with a MSA)
        while (iterator.hasNext()) {
            AuthAtLaunchConfigAuthenticatedUser user = iterator.next();
            if (!Objects.equals(user.getMcUUID(), uuid)) {
                continue;
            }
            iterator.remove();
            removeAuthenticatedUser(user);
        }
    }

    private void removeAuthenticatedUser(AuthAtLaunchConfigAuthenticatedUser authenticatedUser) {
        removeSecret(authenticatedUser.getMicrosoftAccessTokenSecretRef());
        removeSecret(authenticatedUser.getMinecraftAccessTokenSecretRef());
        removeSecret(authenticatedUser.getRefreshTokenSecretRef());
    }

    private UUID addSecret(String secret) {
        UUID uuid = UUID.randomUUID();
        secrets.data.getSecrets().put(uuid, secret);
        markSecretsModified();
        return uuid;
    }

    private void removeSecret(UUID ref) {
        secrets.data.getSecrets().remove(ref);
        markSecretsModified();
    }

    public void hardFailAuthentication(AuthenticationDetails failedMinecraftAuthentication) {
        // Very bad failure - remove the account from future launches
        removeAllAuthenticationsWithUUID(failedMinecraftAuthentication.getMcUUID());
        markConfigModified();
    }

    public boolean isDirty() {
        return config.dirty || secrets.dirty;
    }

    public void markConfigModified() {
        config.markDirty();
    }

    private String getSecret(UUID uuid) {
        return secrets.data.getSecrets().get(uuid);
    }

    public void markSecretsModified() {
        secrets.markDirty();
    }

    @Data
    @AllArgsConstructor
    class ConfigHolder<T extends VersionedConfig> {
        File path;
        T data;
        boolean dirty;

        public void flush() throws AuthAtLaunchConfigurationException {
            if (dirty) {
                data.setUpdatedAt(Instant.now());  // Update the updatedAt timestamp
                try (FileWriter writer = new FileWriter(path)) {
                    gson.toJson(data, writer);
                    dirty = false;  // Reset the dirty flag after successful write
                } catch (IOException e) {
                    throw new AuthAtLaunchConfigurationException("Failed to write config file at " + path.getPath(), e);
                }
            }
        }

        public void markDirty() {
            this.dirty = true;
        }
    }
}
