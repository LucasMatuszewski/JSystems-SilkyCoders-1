package com.sinsay.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DotenvConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();

        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("../")
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envMap = new HashMap<>();

            // Map relevant env vars from .env to Spring properties
            String openrouterModel = dotenv.get("OPENROUTER_MODEL");
            if (openrouterModel != null && !openrouterModel.isBlank()) {
                // Remove quotes if present
                openrouterModel = openrouterModel.replaceAll("^\"|\"$", "");
                envMap.put("openai.model", openrouterModel);
                System.setProperty("openai.model", openrouterModel);
            }

            String openrouterBaseUrl = dotenv.get("OPENROUTER_BASE_URL");
            if (openrouterBaseUrl != null && !openrouterBaseUrl.isBlank()) {
                openrouterBaseUrl = openrouterBaseUrl.replaceAll("^\"|\"$", "");
                envMap.put("openai.base-url", openrouterBaseUrl);
                System.setProperty("openai.base-url", openrouterBaseUrl);
            }

            String openrouterApiKey = dotenv.get("OPENROUTER_API_KEY");
            if (openrouterApiKey != null && !openrouterApiKey.isBlank()) {
                openrouterApiKey = openrouterApiKey.replaceAll("^\"|\"$", "");
                envMap.put("openrouter.api-key", openrouterApiKey);
                System.setProperty("openrouter.api-key", openrouterApiKey);
            }

            if (!envMap.isEmpty()) {
                environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenvProperties", envMap)
                );
                System.out.println("Dotenv: Loaded " + envMap.size() + " properties from .env file");
            }
        } catch (DotenvException e) {
            System.out.println("Dotenv: No .env file found (this is OK if using system env vars)");
        }
    }
}
