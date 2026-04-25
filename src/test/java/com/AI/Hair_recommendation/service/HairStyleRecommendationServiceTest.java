package com.AI.Hair_recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.AI.Hair_recommendation.model.FaceShape;
import com.AI.Hair_recommendation.model.HairStyleRecommendation;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HairStyleRecommendationServiceTest {

    @Autowired
    private HairStyleRecommendationService service;

    @Test
    void returnsRecommendationForKnownFaceShape() {
        assertThat(service.getRecommendations(FaceShape.OVAL)).isNotNull();
    }

    @Test
    void returnsPublicImageUrlsInsteadOfExampleDotComPlaceholders() {
        for (FaceShape faceShape : EnumSet.of(
                FaceShape.OVAL,
                FaceShape.ROUND,
                FaceShape.SQUARE,
                FaceShape.HEART,
                FaceShape.DIAMOND,
                FaceShape.OBLONG
        )) {
            HairStyleRecommendation recommendation = service.getRecommendations(faceShape);

            assertThat(recommendation.getRecommendations())
                    .isNotEmpty()
                    .allSatisfy(style -> {
                        assertThat(style.getImageUrl())
                                .startsWith("https://commons.wikimedia.org/wiki/Special:FilePath/")
                                .doesNotContain("example.com");
                    });
        }
    }
}
