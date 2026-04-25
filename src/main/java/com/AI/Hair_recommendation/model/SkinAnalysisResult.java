package com.AI.Hair_recommendation.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinAnalysisResult {
    private String status;
    private String provider;
    private String summary;
    private String analysisNotes;
    private List<SkinConcernAssessment> concerns;
}
