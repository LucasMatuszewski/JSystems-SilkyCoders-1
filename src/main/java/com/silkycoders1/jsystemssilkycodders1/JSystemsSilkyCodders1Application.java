package com.silkycoders1.jsystemssilkycodders1;

import org.bsc.langgraph4j.agui.AGUIAgent;
import org.bsc.langgraph4j.agui.AGUIAgentExecutor;
import org.bsc.langgraph4j.agui.AGUISampleAgent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.silkycoders1.jsystemssilkycodders1",
	"org.bsc.langgraph4j.agui"
})
public class JSystemsSilkyCodders1Application {

	@Bean("AGUIAgent")
	@ConditionalOnProperty(name = "ag-ui.agent", havingValue = "agentExecutor")
	AGUIAgent createAgentExecutor() {
		return new AGUIAgentExecutor();
	}

	@Bean("AGUIAgent")
	@ConditionalOnProperty(name = "ag-ui.agent", havingValue = "sample")
	AGUIAgent createSampleAgent() {
		return new AGUISampleAgent();
	}

	public static void main(String[] args) {
		SpringApplication.run(JSystemsSilkyCodders1Application.class, args);
	}

}
