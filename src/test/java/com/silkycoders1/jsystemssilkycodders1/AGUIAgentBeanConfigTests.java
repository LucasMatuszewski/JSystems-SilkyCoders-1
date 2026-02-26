package com.silkycoders1.jsystemssilkycodders1;

import org.bsc.langgraph4j.agui.AGUIAgent;
import org.bsc.langgraph4j.agui.AGUIAgentExecutor;
import org.bsc.langgraph4j.agui.AGUISampleAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class AGUIAgentBeanConfigTests {

    @SpringBootTest(
        properties = {
            "ag-ui.agent=agentExecutor",
            "ag-ui.model=OLLAMA_KIMI_K2_5_CLOUD"
        }
    )
    @TestPropertySource(properties = "spring.main.web-application-type=reactive")
    static class AgentExecutorBeanTests {

        @Autowired
        private AGUIAgent aguiAgent;

        @Test
        void shouldCreateAgentExecutorBeanWhenAgentPropertyIsAgentExecutor() {
            assertThat(aguiAgent).isInstanceOf(AGUIAgentExecutor.class);
        }
    }

    @SpringBootTest(
        properties = {
            "ag-ui.agent=sample",
            "ag-ui.model="
        }
    )
    @TestPropertySource(properties = "spring.main.web-application-type=reactive")
    static class SampleAgentBeanTests {

        @Autowired
        private AGUIAgent aguiAgent;

        @Test
        void shouldCreateSampleAgentBeanWhenAgentPropertyIsSample() {
            assertThat(aguiAgent).isInstanceOf(AGUISampleAgent.class);
        }
    }

    @SpringBootTest(
        properties = {
            "ag-ui.agent=agentExecutor",
            "ag-ui.model=OLLAMA_QWEN2_5_7B"
        }
    )
    @TestPropertySource(properties = "spring.main.web-application-type=reactive")
    static class DifferentModelConfigTests {

        @Autowired
        private AGUIAgent aguiAgent;

        @Test
        void shouldAcceptDifferentModelConfig() {
            assertThat(aguiAgent).isInstanceOf(AGUIAgentExecutor.class);
        }
    }
}
