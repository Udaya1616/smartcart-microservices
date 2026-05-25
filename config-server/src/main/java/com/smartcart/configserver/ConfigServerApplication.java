package com.smartcart.configserver;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ConfigServerApplication.class);
        application.setAdditionalProfiles("native");
        application.setDefaultProperties(Map.of(
                "spring.cloud.config.server.native.search-locations", "file:./config-repo,file:../config-repo"
        ));
        application.run(args);
    }
}
