package ai.presight.iamservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "ai.presight",
    exclude = { DataSourceAutoConfiguration.class }
)
public class IamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
