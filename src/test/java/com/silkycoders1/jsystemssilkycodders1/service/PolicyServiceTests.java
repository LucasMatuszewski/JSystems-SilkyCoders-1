package com.silkycoders1.jsystemssilkycodders1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyServiceTests {

    private PolicyService sut;

    @BeforeEach
    void setUp() throws IOException {
        sut = new PolicyService("docs/sinsay-documents");
        sut.init();
    }

    @Test
    void shouldLoadReturnPolicy() {
        String policy = sut.getReturnPolicy();
        assertThat(policy).isNotBlank();
        assertThat(policy).containsIgnoringCase("zwrot");
    }

    @Test
    void shouldLoadComplaintPolicy() {
        String policy = sut.getComplaintPolicy();
        assertThat(policy).isNotBlank();
        assertThat(policy).containsIgnoringCase("reklamacj");
    }

    @Test
    void shouldLoadGeneralTerms() {
        String policy = sut.getGeneralTerms();
        assertThat(policy).isNotBlank();
        assertThat(policy).containsIgnoringCase("Regulamin");
    }

    @Test
    void shouldReturnCombinedReturnAndGeneralTermsForReturnIntent() {
        String policies = sut.getPoliciesForIntent("return");
        assertThat(policies).isNotBlank();
        assertThat(policies).containsIgnoringCase("zwrot");
        assertThat(policies).containsIgnoringCase("Regulamin");
    }

    @Test
    void shouldReturnCombinedComplaintAndGeneralTermsForComplaintIntent() {
        String policies = sut.getPoliciesForIntent("complaint");
        assertThat(policies).isNotBlank();
        assertThat(policies).containsIgnoringCase("reklamacj");
        assertThat(policies).containsIgnoringCase("Regulamin");
    }

    @Test
    void shouldReturnEmptyStringForNullIntent() {
        String policies = sut.getPoliciesForIntent(null);
        assertThat(policies).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringForBlankIntent() {
        String policies = sut.getPoliciesForIntent("");
        assertThat(policies).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringForUnknownIntent() {
        String policies = sut.getPoliciesForIntent("unknown_intent");
        assertThat(policies).isEmpty();
    }
}
