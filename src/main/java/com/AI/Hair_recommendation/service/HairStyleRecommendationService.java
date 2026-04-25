package com.AI.Hair_recommendation.service;

import com.AI.Hair_recommendation.model.FaceShape;
import com.AI.Hair_recommendation.model.HairStyleRecommendation;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HairStyleRecommendationService {

    private Map<FaceShape, HairStyleRecommendation> recommendationDatabase;

    @PostConstruct
    public void loadRecommendations() {
        // Initialize with hardcoded recommendations
        // In production, load from JSON file or database
        recommendationDatabase = new HashMap<>();

        recommendationDatabase.put(FaceShape.OVAL, createOvalRecommendations());
        recommendationDatabase.put(FaceShape.ROUND, createRoundRecommendations());
        recommendationDatabase.put(FaceShape.SQUARE, createSquareRecommendations());
        recommendationDatabase.put(FaceShape.HEART, createHeartRecommendations());
        recommendationDatabase.put(FaceShape.DIAMOND, createDiamondRecommendations());
        recommendationDatabase.put(FaceShape.OBLONG, createOblongRecommendations());
    }

    public HairStyleRecommendation getRecommendations(FaceShape faceShape) {
        return recommendationDatabase.getOrDefault(faceShape, createDefaultRecommendations());
    }

    private HairStyleRecommendation createOvalRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "Textured Crop",
                        "Short sides with textured top",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/High_top_fade.jpg",
                        "Oval faces are balanced and work with almost any style. Textured crop adds modern edge."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Side Part",
                        "Classic side part with medium length",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Haircut.jpg",
                        "Maintains natural balance and adds sophistication."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Quiff",
                        "Volume on top with shorter sides",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Man_with_glasses_and_a_quiff_hairstyle_%281490855%29.jpg",
                        "Adds height while maintaining proportions."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.OVAL,
                styles,
                "Your oval face shape is well-balanced, giving you flexibility with most hairstyles."
        );
    }

    private HairStyleRecommendation createRoundRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "High Fade with Volume",
                        "Short sides, voluminous top",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/High_top_fade.jpg",
                        "Creates vertical lines to elongate your face shape."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Angular Fringe",
                        "Asymmetric fringe adds angles",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Bangs.jpg",
                        "Angular cuts create definition and structure."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Pompadour",
                        "Classic pompadour with height",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Pompadour_hairstyle.jpg",
                        "Height on top adds length to face proportions."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.ROUND,
                styles,
                "Round faces benefit from styles that add height and create angles."
        );
    }

    private HairStyleRecommendation createSquareRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "Textured Waves",
                        "Soft waves that soften jawline",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Wavy_hair_%2851213255008%29.jpg",
                        "Softens strong angular features naturally."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Side Sweep",
                        "Swept fringe to one side",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Wavy_hair_%2851213040716%29.jpg",
                        "Creates diagonal lines to balance strong jaw."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Layered Medium Length",
                        "Layers add movement and softness",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Haircuts.jpg",
                        "Movement and texture soften angular features."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.SQUARE,
                styles,
                "Square faces look great with styles that add softness and curves."
        );
    }

    private HairStyleRecommendation createHeartRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "Side Part with Volume",
                        "Volume at sides balances forehead",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Haircut.jpg",
                        "Adds width at jaw level to balance wider forehead."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Chin-Length Cut",
                        "Ends at chin to add width",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Hair_cut.jpg",
                        "Creates balance by widening at the jawline."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.HEART,
                styles,
                "Heart-shaped faces benefit from styles that balance a wider forehead with a narrower chin."
        );
    }

    private HairStyleRecommendation createDiamondRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "Swept Back",
                        "Swept back to showcase cheekbones",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Bethporterselfphoto.png",
                        "Highlights your best feature - cheekbones."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Short Sides Long Top",
                        "Contrast emphasizes bone structure",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/High_top_fade.jpg",
                        "Creates width at forehead to balance narrow chin."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.DIAMOND,
                styles,
                "Diamond faces have great cheekbones - highlight them!"
        );
    }

    private HairStyleRecommendation createOblongRecommendations() {
        List<HairStyleRecommendation.StyleOption> styles = Arrays.asList(
                new HairStyleRecommendation.StyleOption(
                        "Fringe/Bangs",
                        "Horizontal fringe shortens face visually",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Bangs.jpg",
                        "Breaks up vertical length of face."
                ),
                new HairStyleRecommendation.StyleOption(
                        "Side Volume",
                        "Width at sides balances length",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Wavy_hair_%2851213040716%29.jpg",
                        "Adds width to balance facial proportions."
                )
        );
        return new HairStyleRecommendation(
                FaceShape.OBLONG,
                styles,
                "Oblong faces benefit from styles that add width and minimize length."
        );
    }

    private HairStyleRecommendation createDefaultRecommendations() {
        return new HairStyleRecommendation(
                FaceShape.UNKNOWN,
                Collections.emptyList(),
                "Unable to determine face shape. Please try with a clearer front-facing photo."
        );
    }
}
