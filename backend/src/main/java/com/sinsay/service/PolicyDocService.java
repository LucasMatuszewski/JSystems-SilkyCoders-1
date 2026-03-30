package com.sinsay.service;

import com.sinsay.model.Intent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PolicyDocService {

    @Value("${policy-docs.path:../docs}")
    private String policyDocsPath;

    /**
     * Sets the policy docs path (for testing purposes).
     */
    public void setPolicyDocsPath(String path) {
        this.policyDocsPath = path;
    }

    /**
     * Assembles the system prompt for the LLM based on the intent.
     * Includes role definition, decision categories, disclaimer, scope boundary,
     * language instruction, and relevant policy document content.
     *
     * @param intent The user's intent (RETURN or COMPLAINT)
     * @return Complete system prompt string
     */
    public String getSystemPrompt(Intent intent) {
        StringBuilder prompt = new StringBuilder();

        // 1. Role definition
        prompt.append("You are an AI assistant for Sinsay online store. ");
        prompt.append("Your purpose is to help customers estimate whether their return (zwrot) or complaint (reklamacja) ");
        prompt.append("is likely to be accepted based on Sinsay's policies and the product photo provided.\n\n");

        // 2. Decision categories
        prompt.append("Decision categories:\n");
        prompt.append("- Prawdopodobnie zaakceptowane (Likely accepted)\n");
        prompt.append("- Prawdopodobnie odrzucone (Likely rejected)\n");
        prompt.append("- Niejasne - wymaga recenzji ręcznej (Unclear - requires manual review)\n\n");

        // 3. Mandatory disclaimer
        prompt.append("IMPORTANT DISCLAIMER: This assessment is not legally binding. ");
        prompt.append("The final decision is always made by a human Sinsay customer support agent. ");
        prompt.append("This is only an estimate based on the provided information and Sinsay's policies.\n\n");

        // 4. Scope boundary
        prompt.append("Scope: Answer questions about Sinsay policies, return/complaint procedures, and related topics. ");
        prompt.append("Redirect off-topic questions politely.\n\n");

        // 5. Language instruction
        prompt.append("LANGUAGE: Always respond in Polish.\n\n");

        // 6. Policy document content
        prompt.append("--- POLICY DOCUMENTS ---\n\n");

        // Always include regulamin
        prompt.append(readPolicyFile("regulamin.md"));
        prompt.append("\n\n");

        // Intent-specific document
        if (intent == Intent.RETURN) {
            prompt.append(readPolicyFile("zwrot-30-dni.md"));
        } else if (intent == Intent.COMPLAINT) {
            prompt.append(readPolicyFile("reklamacje.md"));
        }

        return prompt.toString();
    }

    /**
     * Reads a policy markdown file from the policy docs directory.
     *
     * @param filename The name of the file to read
     * @return The file content as a String
     * @throws IllegalStateException if the file cannot be read
     */
    private String readPolicyFile(String filename) {
        try {
            Path filePath = Paths.get(policyDocsPath, filename);
            if (!Files.exists(filePath)) {
                throw new IllegalStateException("Policy file not found: " + filename + " (looked in: " + policyDocsPath + ")");
            }
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read policy file: " + filename, e);
        }
    }
}
