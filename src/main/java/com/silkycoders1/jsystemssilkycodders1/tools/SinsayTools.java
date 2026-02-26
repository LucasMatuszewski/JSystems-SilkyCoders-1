package com.silkycoders1.jsystemssilkycodders1.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SinsayTools {

    /**
     * Tool called by the LLM when the user wants to return a product or file a complaint.
     * The AG-UI protocol renders the return/complaint form in the chat UI via TOOL_CALL events.
     * The form response (user's submission) is returned as the tool result.
     *
     * @param type "return" or "complaint"
     * @return the type, echoed back — actual form data comes from the AG-UI frontend response
     */
    @Tool(description = "Show a return or complaint form to the user. Call this when the user wants to return a product or file a complaint.")
    public String showReturnForm(
            @ToolParam(description = "Type of request: 'return' or 'complaint'") String type
    ) {
        return type;
    }
}
