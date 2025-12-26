package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractAnalysisRequestDto {
    private String text; // 계약서 원문 텍스트

    public String getFullContractText() {
        return "";
    }
}