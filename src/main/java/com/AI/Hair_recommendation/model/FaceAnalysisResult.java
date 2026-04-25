package com.AI.Hair_recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceAnalysisResult {
    private FaceShape faceShape;
    private double faceLength;
    private double faceWidth;
    private double jawWidth;
    private double foreheadWidth;
    private String analysisNotes;
}
