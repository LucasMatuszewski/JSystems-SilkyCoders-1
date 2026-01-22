package com.silkycoders1.jsystemssilkycodders1.api.reuqest;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatGPTRequest(
        String model,
        List<Message> messages) {

    public record Message(String role, Object content) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ContentPart(String type, String text, ImageUrl image_url) {
        // Constructor for text
        public ContentPart(String text) {
            this("text", text, null);
        }

        // Constructor for image
        public ContentPart(ImageUrl imageUrl) {
            this("image_url", null, imageUrl);
        }
    }

    public record ImageUrl(String url) {
    }
}
