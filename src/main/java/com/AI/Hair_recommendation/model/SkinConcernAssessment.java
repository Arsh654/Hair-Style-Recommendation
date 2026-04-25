package com.AI.Hair_recommendation.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinConcernAssessment {
    private String concern;
    private int score;
    private String label;
    private String severityColor;
    private String explanation;
    private List<String> remedies;
    private List<ProductSuggestion> productSuggestions;
}
