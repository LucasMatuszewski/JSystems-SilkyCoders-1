package com.sinsay.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DotenvInitializer implements EnvironmentAware {

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof StandardEnvironment standardEnv) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory("../")
                        .ignoreIfMissing()
                        .load();

                Map<String, Object> envMap = new HashMap<>();

                // Map OPENROUTER_MODEL -> openai.model (with quote stripping)
                String openrouterModel = dotenv.get("OPENROUTER_MODEL");
                if (openrouterModel != null && !openrouterModel.isBlank()) {
                    // Remove quotes if present
                    openrouterModel = openrouterModel.replaceAll("^\"|\"$", "");
                    envMap.put("openai.model", openrouterModel);
                    System.setProperty("openai.model", openrouterModel);
                    System.out.println("Dotenv: Set openai.model = " + openrouterModel);
                }

                // Map OPENROUTER_BASE_URL -> openai.base-url
                String openrouterBaseUrl = dotenv.get("OPENROUTER_BASE_URL");
                if (openrouterBaseUrl != null && !openrouterBaseUrl.isBlank()) {
                    openrouterBaseUrl = openrouterBaseUrl.replaceAll("^\"|\"$", "");
                    envMap.put("openai.base-url", openrouterBaseUrl);
                    System.setProperty("openai.base-url", openrouterBaseUrl);
                    System.out.println("Dotenv: Set openai.base-url = " + openrouterBaseUrl);
                }

                // Map OPENROUTER_API_KEY -> openrouter.api-key
                String openrouterApiKey = dotenv.get("OPENROUTER_API_KEY");
                if (openrouterApiKey != null && !openrouterApiKey.isBlank()) {
                    openrouterApiKey = openrouterApiKey.replaceAll("^\"|\"$", "");
                    envMap.put("openrouter.api-key", openrouterApiKey);
                    System.setProperty("openrouter.api-key", openrouterApiKey);
                    System.out.println("Dotenv: Set openrouter.api-key = " + openrouterApiKey.substring(0, Math.min(20, openrouterApiKey.length())) + "...");
                }

                if (!envMap.isEmpty()) {
                    PropertySource<?> propertySource = new MapPropertySource("dotenvProperties", envMap);
                    standardEnv.getPropertySources().addFirst(propertySource);
                    System.out.println("Dotenv: Loaded " + envMap.size() + " properties from .env file");
                }
            } catch (DotenvException e) {
                System.out.println("Dotenv: No .env file found (using system env vars or defaults)");
            }
        }
    }
}
