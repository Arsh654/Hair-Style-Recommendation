package com.AI.Hair_recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HairStyleRecommendation {
    private FaceShape faceShape;
    private List<StyleOption> recommendations;
    private String reasoning;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StyleOption {
        private String name;
        private String description;
        private String imageUrl;
        private String whyItWorks;
    }
}
