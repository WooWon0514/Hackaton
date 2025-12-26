package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedQuestionDto {
    // 프롬프트 예시의 JSON key와 일치시킵니다.
    private String badge;
    private String question;
}