package ai.presight.iamservice.service;

import ai.presight.iamservice.config.UserConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserConfig userConfig;

    public boolean validateUser(String username, String password) {
        return userConfig.getUsers().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password));
    }
}
