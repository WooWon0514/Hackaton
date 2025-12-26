// src/main/java/com/example/demo/dto/openai/OpenAIChatRequest.java
package com.example.demo.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenAIChatRequest {

    private String model;
    private List<Message> messages;
    @JsonProperty("response_format") // JSON 필드 이름을 response_format으로 지정
    private ResponseFormat responseFormat;
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // 생성자
    public OpenAIChatRequest(String model, List<Message> messages, ResponseFormat responseFormat) {
        this.model = model;
        this.messages = messages;
        this.responseFormat = responseFormat;
    }

    public OpenAIChatRequest(String model, List<Message> messages, Double temperature, Integer maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    // Getter와 Setter
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public ResponseFormat getResponseFormat() { return responseFormat; }
    public void setResponseFormat(ResponseFormat responseFormat) { this.responseFormat = responseFormat; }

    // 내부 클래스 (static으로 선언하여 외부 클래스 인스턴스 없이 생성 가능)
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ResponseFormat {
        private String type;

        public ResponseFormat(String type) {
            this.type = type;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}