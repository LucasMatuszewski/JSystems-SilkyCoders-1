package com.silkycoders1.jsystemssilkycodders1.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final String policyDir;

    private String returnPolicy;
    private String complaintPolicy;
    private String generalTerms;

    public PolicyService(@Value("${sinsay.policy.dir:docs/sinsay-documents}") String policyDir) {
        this.policyDir = policyDir;
    }

    @PostConstruct
    public void init() throws IOException {
        log.info("Loading Sinsay policy documents from: {}", policyDir);
        returnPolicy = Files.readString(Path.of(policyDir, "zwrot-30-dni.md"));
        complaintPolicy = Files.readString(Path.of(policyDir, "reklamacje.md"));
        generalTerms = Files.readString(Path.of(policyDir, "regulamin.md"));
        log.info("Loaded {} chars return policy, {} chars complaint policy, {} chars general terms",
                returnPolicy.length(), complaintPolicy.length(), generalTerms.length());
    }

    public String getReturnPolicy() {
        return returnPolicy;
    }

    public String getComplaintPolicy() {
        return complaintPolicy;
    }

    public String getGeneralTerms() {
        return generalTerms;
    }

    /**
     * Returns the policy documents relevant to the given intent.
     *
     * @param intent "return" → zwrot-30-dni + regulamin, "complaint" → reklamacje + regulamin,
     *               null/blank/unknown → empty string (general conversation, no policy injection)
     */
    public String getPoliciesForIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return "";
        }
        return switch (intent.toLowerCase().trim()) {
            case "return" -> returnPolicy + "\n\n---\n\n" + generalTerms;
            case "complaint" -> complaintPolicy + "\n\n---\n\n" + generalTerms;
            default -> "";
        };
    }
}
