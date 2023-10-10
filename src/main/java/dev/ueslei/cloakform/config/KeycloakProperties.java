package dev.ueslei.cloakform.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.OAuth2Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    @NotEmpty
    private String serverUrl = "http://localhost:8181/auth";
    @NotEmpty
    private String realm = "master";
    @NotEmpty
    private String clientId = "admin-cli";
    private String clientSecret;
    private String username = "admin";
    private String password = "admin";
    @NotEmpty
    private String grantType = OAuth2Constants.PASSWORD;
    private String authorization;
    private String scope;

    /**
     * Proxy configuration. Empty by default.
     */
    private Proxy proxy = new Proxy(null, -1, null);

    public record Proxy(String host, Integer port, String scheme) {

    }

}
