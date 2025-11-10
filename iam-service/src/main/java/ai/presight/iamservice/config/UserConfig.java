package ai.presight.iamservice.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * Loads static credentials from application.yaml.
 * Later this will be replaced by UAEPASS / LDAP integration.
 */


@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConfig {

    private List<UserCredential> users;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserCredential {
        private String username;
        private String password;
    }
}
