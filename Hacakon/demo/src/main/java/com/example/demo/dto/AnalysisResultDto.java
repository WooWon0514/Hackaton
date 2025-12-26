package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDto {

    @JsonProperty("criteria_id")
    private int criteriaId;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("badge_color")
    private String badgeColor;

    @JsonProperty("badge_label")
    private String badgeLabel;

    @JsonProperty("original_text")
    private String originalText;

    @JsonProperty("issue_summary")
    private String issueSummary;

    private String reasoning;
}