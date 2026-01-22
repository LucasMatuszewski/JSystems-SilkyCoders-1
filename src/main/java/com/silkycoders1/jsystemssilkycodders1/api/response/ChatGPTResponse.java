package com.silkycoders1.jsystemssilkycodders1.api.response;

import java.util.List;

public record ChatGPTResponse(
        List<Choice> choices) {
    public record Choice(Message message) {
        public record Message(String content) {
        }
    }
}
