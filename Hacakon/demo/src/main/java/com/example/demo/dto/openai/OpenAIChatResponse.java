package com.example.demo.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// 우리가 사용하지 않는 필드는 무시하도록 설정
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIChatResponse {

    private List<Choice> choices;

    // Getter와 Setter
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    // 내부 클래스들
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}