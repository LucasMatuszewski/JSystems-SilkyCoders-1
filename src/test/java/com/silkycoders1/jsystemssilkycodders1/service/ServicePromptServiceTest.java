package com.silkycoders1.jsystemssilkycodders1.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServicePromptServiceTest {

    private final ServicePromptService servicePromptService = new ServicePromptService();

    @Test
    void shouldBuildPromptWithProductDetails() {
        // given
        String productDetails = "Rozdarty szew w lewym rękawie kurtki zimowej.";

        // when
        String prompt = servicePromptService.buildImageAnalysisPrompt(productDetails);

        // then
        assertThat(prompt).as("Prompt powinien zawierać szczegóły problemu podane przez użytkownika")
                .contains(productDetails);
    }

    @Test
    void shouldContainKeySectionsInPrompt() {
        // given
        String productDetails = "Jakikolwiek detal";

        // when
        String prompt = servicePromptService.buildImageAnalysisPrompt(productDetails);

        // then
        assertThat(prompt).contains("KRYTERIA AUTOMATYCZNEJ AKCEPTACJI REKLAMACJI")
                .contains("KRYTERIA AUTOMATYCZNEGO ODRZUCENIA REKLAMACJI")
                .contains("ANALIZA I FORMAT ODPOWIEDZI")
                .contains("can_be_claimed");
    }
}
