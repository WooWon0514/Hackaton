package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCompositionRequestDto {
    // 프론트엔드에서 이 key로 데이터를 보내게 됩니다.
    @JsonProperty("selected_questions")
    private List<SelectedQuestionDto> selectedQuestions;
}