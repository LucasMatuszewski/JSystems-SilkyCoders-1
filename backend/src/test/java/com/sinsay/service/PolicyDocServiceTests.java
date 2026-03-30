package com.sinsay.service;

import com.sinsay.model.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PolicyDocServiceTests {

    @Autowired
    private PolicyDocService policyDocService;

    @TempDir
    Path tempDir;

    private Path regulaminPath;
    private Path reklamacjePath;
    private Path zwrotPath;

    @BeforeEach
    void setUp() throws IOException {
        // Create temp directory structure for policy docs
        regulaminPath = tempDir.resolve("regulamin.md");
        reklamacjePath = tempDir.resolve("reklamacje.md");
        zwrotPath = tempDir.resolve("zwrot-30-dni.md");

        // Write test content with unique markers (using actual unique content from real docs)
        Files.writeString(regulaminPath, "# REGULAMIN UNIQUE MARKER ABC123\nLPP S.A. z siedzibą w Gdańsku\n");
        Files.writeString(reklamacjePath, "# REKLAMACJA UNIQUE MARKER DEF456\nZamówione produkty możesz reklamować w ciągu 2 lat\n");
        Files.writeString(zwrotPath, "# ZWROT UNIQUE MARKER GHI789\nPo otrzymaniu przesyłki masz 30 dni\n");

        // Update the service's path to point to temp directory
        policyDocService.setPolicyDocsPath(tempDir.toString());
    }

    @Test
    void getSystemPrompt_withReturnIntent_shouldContainRegulaminAndZwrot_notReklamacje() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.RETURN);

        // Then - should contain role definition
        assertThat(prompt).contains("Sinsay online store");
        assertThat(prompt).contains("zwrot");
        assertThat(prompt).contains("reklamacja");

        // Should contain regulamin unique marker
        assertThat(prompt).contains("REGULAMIN UNIQUE MARKER ABC123");
        assertThat(prompt).contains("LPP S.A. z siedzibą w Gdańsku");

        // Should contain zwrot unique marker
        assertThat(prompt).contains("ZWROT UNIQUE MARKER GHI789");
        assertThat(prompt).contains("otrzymaniu przesyłki masz 30 dni");

        // Should NOT contain reklamacje unique marker (TAC-BE-01)
        assertThat(prompt).doesNotContain("REKLAMACJA UNIQUE MARKER DEF456");
        assertThat(prompt).doesNotContain("możesz reklamować"); // unique substring from reklamacje.md
    }

    @Test
    void getSystemPrompt_withComplaintIntent_shouldContainRegulaminAndReklamacje_notZwrot() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.COMPLAINT);

        // Then - should contain role definition
        assertThat(prompt).contains("Sinsay online store");
        assertThat(prompt).contains("zwrot");
        assertThat(prompt).contains("reklamacja");

        // Should contain regulamin unique marker
        assertThat(prompt).contains("REGULAMIN UNIQUE MARKER ABC123");
        assertThat(prompt).contains("LPP S.A. z siedzibą w Gdańsku");

        // Should contain reklamacje unique marker
        assertThat(prompt).contains("REKLAMACJA UNIQUE MARKER DEF456");
        assertThat(prompt).contains("Zamówione produkty możesz reklamować");

        // Should NOT contain zwrot unique marker (TAC-BE-02)
        assertThat(prompt).doesNotContain("ZWROT UNIQUE MARKER GHI789");
        assertThat(prompt).doesNotContain("otrzymaniu przesyłki masz 30 dni");
    }

    @Test
    void getSystemPrompt_shouldContainRoleDefinition() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.RETURN);

        // Then
        assertThat(prompt).contains("AI assistant");
        assertThat(prompt).contains("Sinsay");
    }

    @Test
    void getSystemPrompt_shouldContainMandatoryDisclaimer() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.RETURN);

        // Then
        assertThat(prompt).contains("not legally binding");
        assertThat(prompt).contains("final decision");
    }

    @Test
    void getSystemPrompt_shouldContainLanguageInstruction() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.RETURN);

        // Then
        assertThat(prompt).contains("Polish");
    }

    @Test
    void getSystemPrompt_shouldContainDecisionCategories() {
        // When
        String prompt = policyDocService.getSystemPrompt(Intent.RETURN);

        // Then
        assertThat(prompt).contains("zaakceptowane");
        assertThat(prompt).contains("odrzucone");
    }

    @Test
    void getSystemPrompt_whenRegulaminMissing_shouldThrowDescriptiveException() throws IOException {
        // Given
        Files.deleteIfExists(regulaminPath);

        // When/Then
        assertThatThrownBy(() -> policyDocService.getSystemPrompt(Intent.RETURN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("regulamin.md");
    }

    @Test
    void getSystemPrompt_whenIntentSpecificDocMissing_shouldThrowDescriptiveException() throws IOException {
        // Given
        Files.deleteIfExists(zwrotPath);

        // When/Then
        assertThatThrownBy(() -> policyDocService.getSystemPrompt(Intent.RETURN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zwrot-30-dni.md");
    }
}
