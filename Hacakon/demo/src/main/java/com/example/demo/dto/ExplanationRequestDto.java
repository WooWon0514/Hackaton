package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 기존의 issueTitle, issueContent를 issueSummary, originalText로 변경
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationRequestDto {
    private String issueSummary;
    private String originalText;
}