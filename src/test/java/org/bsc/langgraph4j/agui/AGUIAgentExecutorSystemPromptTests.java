package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AGUIAgentExecutorSystemPromptTests {

    @Mock
    private PolicyService policyService;

    private SinsayTools sinsayTools;
    private AGUIAgentExecutor sut;

    @BeforeEach
    void setUp() {
        sinsayTools = new SinsayTools();
        sut = new AGUIAgentExecutor(null, policyService, sinsayTools);
    }

    @Test
    void shouldIncludeBaseAssistantRoleInSystemPrompt() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        String prompt = sut.buildSystemPrompt("");

        assertThat(prompt).containsIgnoringCase("Sinsay");
        assertThat(prompt).containsIgnoringCase("assistant");
    }

    @Test
    void shouldIncludeWelcomeMessageInstructionInSystemPrompt() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        String prompt = sut.buildSystemPrompt("");

        assertThat(prompt).containsIgnoringCase("Cześć");
    }

    @Test
    void shouldIncludeToolUsageInstructionInSystemPrompt() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        String prompt = sut.buildSystemPrompt("");

        assertThat(prompt).contains("showReturnForm");
    }

    @Test
    void shouldIncludeReturnPolicyWhenIntentIsReturn() {
        String returnPolicyContent = "ZWROT_POLICY_CONTENT_30_DNI";
        when(policyService.getPoliciesForIntent("return")).thenReturn(returnPolicyContent);

        String prompt = sut.buildSystemPrompt("return");

        assertThat(prompt).contains(returnPolicyContent);
    }

    @Test
    void shouldIncludeComplaintPolicyWhenIntentIsComplaint() {
        String complaintPolicyContent = "REKLAMACJA_POLICY_CONTENT";
        when(policyService.getPoliciesForIntent("complaint")).thenReturn(complaintPolicyContent);

        String prompt = sut.buildSystemPrompt("complaint");

        assertThat(prompt).contains(complaintPolicyContent);
    }

    @Test
    void shouldNotIncludePolicySectionWhenIntentIsEmpty() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        String prompt = sut.buildSystemPrompt("");

        // Policy section header should not appear when no policies loaded
        assertThat(prompt).doesNotContain("POLICY DOCUMENTS");
    }

    @Test
    void shouldIncludePolicySectionHeaderWhenPoliciesArePresent() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("some policy");

        String prompt = sut.buildSystemPrompt("return");

        assertThat(prompt).containsIgnoringCase("policy");
    }
}
