package com.AI.Hair_recommendation.service;

import com.AI.Hair_recommendation.model.SkinAnalysisResult;
import com.AI.Hair_recommendation.model.ProductSuggestion;
import com.AI.Hair_recommendation.model.SkinConcernAssessment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SkinAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(SkinAnalysisService.class);
    private static final String PROVIDER = "Groq";
    private static final String DEFAULT_VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    private static final Set<String> SUPPORTED_VISION_MODELS = Set.of(
            DEFAULT_VISION_MODEL,
            "meta-llama/llama-4-maverick-17b-128e-instruct"
    );
    private static final List<String> SUPPORTED_CONCERNS = List.of(
            "Acne",
            "Pigmentation",
            "Uniformness",
            "Pores",
            "Dark Circles",
            "Redness",
            "Skin Tone",
            "Lines",
            "Hydration"
    );
    private static final ProductSuggestion SUNSCREEN = new ProductSuggestion(
            "Minimalist SPF 50 Sunscreen",
            "Sunscreen",
            "Broad-spectrum daily UV protection supports tone, pigmentation control, and post-acne mark prevention.",
            "https://beminimalist.co/products/multi-vitamin-spf-50"
    );
    private static final ProductSuggestion CLEANSER = new ProductSuggestion(
            "Cetaphil Oily Skin Cleanser",
            "Cleanser",
            "A gentle foaming cleanser helps reduce surface oil and buildup without stripping the barrier.",
            "https://www.cetaphil.in/products/cleansers/oily-skin-cleanser/8906005274090.html/"
    );
    private static final ProductSuggestion NIACINAMIDE = new ProductSuggestion(
            "Minimalist Niacinamide 10% Face Serum",
            "Serum",
            "Niacinamide is commonly used for visible oil control, pores, and post-blemish uneven tone.",
            "https://beminimalist.co/collections/treatments/products/niacinamide-10-with-matmarine"
    );
    private static final ProductSuggestion EYE_SERUM = new ProductSuggestion(
            "Minimalist Vitamin K + Retinal 1% Eye Cream",
            "Eye Serum",
            "A caffeine-based eye serum is a practical option when under-eye puffiness or darkness is visible.",
            "https://beminimalist.co/products/vitamin-k-retinal-01-eye-cream"
    );
    private static final ProductSuggestion MOISTURIZER = new ProductSuggestion(
            "Minimalist Vitamin B5 10% Moisturizer",
            "Moisturizer",
            "Barrier-supportive moisturizers help with dehydration, surface roughness, and irritation-prone skin.",
            "https://beminimalist.co/products/vitamin-b5-10-moisturizer"
    );
    private static final ProductSuggestion BHA = new ProductSuggestion(
            "Minimalist Salicylic Acid 2% Face Serum",
            "Exfoliant",
            "A salicylic-acid exfoliant is commonly used for clogged pores, uneven texture, and blemish-prone areas.",
            "https://beminimalist.co/products/salicylic-acid-2"
    );

    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String responsesUrl;

    @Autowired
    public SkinAnalysisService(
            @Value("${groq.api.key:}") String groqApiKey,
            @Value("${groq.skin.model:" + DEFAULT_VISION_MODEL + "}") String groqModel,
            @Value("${groq.responses.url:https://api.groq.com/openai/v1/chat/completions}") String groqResponsesUrl,
            ObjectMapper objectMapper
    ) {
        this(
                new RestTemplate(),
                objectMapper,
                groqApiKey,
                groqModel,
                groqResponsesUrl
        );
    }

    SkinAnalysisService(
            RestOperations restOperations,
            ObjectMapper objectMapper,
            String apiKey,
            String model,
            String responsesUrl
    ) {
        this.restOperations = restOperations;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.responsesUrl = responsesUrl;
    }

    public SkinAnalysisResult analyzeSkin(MultipartFile imageFile) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            return unavailableResult("Skin analysis is disabled. Set GROQ_API_KEY to enable AI vision scoring.");
        }

        String effectiveModel = resolveVisionModel();
        log.info("Skin analysis request starting with provider={}, model={}, endpoint={}", PROVIDER, effectiveModel, responsesUrl);
        try {
            String payload = buildRequestPayload(imageFile, effectiveModel);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restOperations.postForEntity(
                    responsesUrl,
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            log.info("Groq skin analysis response received successfully");
            return mapResponse(response.getBody());
        } catch (RestClientResponseException exception) {
            log.error(
                    "Groq skin analysis API error status={} body={}",
                    exception.getStatusCode(),
                    truncate(exception.getResponseBodyAsString()),
                    exception
            );
            return failedResult("Skin analysis could not complete right now. Check the configured Groq API key and retry.");
        } catch (Exception exception) {
            log.error("Groq skin analysis failed before a valid response was parsed", exception);
            return failedResult("Skin analysis could not complete right now. Check the configured Groq API key and retry.");
        }
    }

    private String buildRequestPayload(MultipartFile imageFile, String effectiveModel) throws IOException {
        String contentType = imageFile.getContentType() == null ? MediaType.IMAGE_JPEG_VALUE : imageFile.getContentType();
        String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
        String imageDataUrl = "data:" + contentType + ";base64," + base64Image;

        Map<String, Object> request = Map.of(
                "model", effectiveModel,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        You are a skincare selfie analyzer. Score only visible skin concerns from the provided facial photo.
                                        Return valid JSON only. Keep scores between 0 and 100 where higher means the concern is more visible.
                                        Provide one short explanation per concern. Do not diagnose medical conditions.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", """
                                                        Analyze these nine concerns exactly once each: Acne, Pigmentation, Uniformness, Pores, Dark Circles, Redness, Skin Tone, Lines, Hydration.
                                                        Return a JSON object with:
                                                        - summary: string
                                                        - concerns: array of 9 objects with concern, score, explanation
                                                        """
                                        ),
                                        Map.of(
                                                "type", "image_url",
                                                "image_url", Map.of(
                                                        "url", imageDataUrl
                                                )
                                        )
                                )
                        )
                )
        );

        return objectMapper.writeValueAsString(request);
    }

    private String resolveVisionModel() {
        if (model != null && SUPPORTED_VISION_MODELS.contains(model)) {
            return model;
        }

        log.warn(
                "Configured Groq skin model '{}' does not support image analysis. Falling back to '{}'.",
                model,
                DEFAULT_VISION_MODEL
        );
        return DEFAULT_VISION_MODEL;
    }

    private SkinAnalysisResult mapResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("choices").path(0).path("message").path("content");
        if (textNode.isMissingNode() || textNode.isNull()) {
            log.error("Groq skin analysis response missing content: {}", truncate(responseBody));
            return failedResult("Skin analysis response was empty. Please try again with a clear front-facing photo.");
        }

        JsonNode analysisNode = objectMapper.readTree(textNode.asText());
        List<SkinConcernAssessment> concerns = new ArrayList<>();
        for (JsonNode concernNode : analysisNode.path("concerns")) {
            String concern = concernNode.path("concern").asText();
            int score = concernNode.path("score").asInt();
            concerns.add(new SkinConcernAssessment(
                    concern,
                    score,
                    scoreLabel(score),
                    severityColor(score),
                    concernNode.path("explanation").asText(),
                    remediesFor(concern, score),
                    productSuggestionsFor(concern, score)
            ));
        }

        return new SkinAnalysisResult(
                "READY",
                PROVIDER,
                analysisNode.path("summary").asText(),
                "AI vision analysis from a single selfie. Scores reflect visible appearance only.",
                concerns
        );
    }

    private String scoreLabel(int score) {
        if (score < 35) {
            return "Low";
        }
        if (score < 65) {
            return "Balanced";
        }
        return "High";
    }

    private String severityColor(int score) {
        if (score < 35) {
            return "green";
        }
        if (score < 65) {
            return "amber";
        }
        return "coral";
    }

    private List<String> remediesFor(String concern, int score) {
        return switch (concern) {
            case "Acne" -> List.of(
                    "Cleanse gently twice daily and avoid scrubbing active breakouts.",
                    "Keep leave-on exfoliation limited to a few nights per week instead of stacking strong actives.",
                    "Choose non-comedogenic sunscreen and avoid picking inflamed spots."
            );
            case "Pigmentation" -> List.of(
                    "Use broad-spectrum sunscreen every morning and reapply when exposed to daylight.",
                    "Add a tone-evening serum consistently instead of rotating too many brightening products.",
                    "Avoid picking blemishes or irritation triggers that can deepen leftover marks."
            );
            case "Uniformness", "Skin Tone" -> List.of(
                    "Prioritize daily sunscreen because UV exposure quickly worsens uneven tone.",
                    "Keep the barrier steady with a simple moisturizer and consistent routine.",
                    "Use one brightening or oil-balancing serum consistently rather than changing products often."
            );
            case "Pores" -> List.of(
                    "Use a gentle cleanser and keep excess oil under control without over-washing.",
                    "A salicylic-acid exfoliant a few times per week can help with congestion-prone areas.",
                    "Lightweight hydration often helps skin look smoother than aggressive stripping."
            );
            case "Dark Circles" -> List.of(
                    "Improve sleep regularity and reduce rubbing around the eye area.",
                    "Use a lightweight eye serum and keep the under-eye area hydrated.",
                    "Daily sunscreen around the face helps prevent worsening contrast and uneven tone."
            );
            case "Redness" -> List.of(
                    "Reduce harsh exfoliation and avoid layering too many strong actives together.",
                    "Focus on barrier support with a simple moisturizer and gentle cleanser.",
                    "Use sunscreen daily to reduce visible flare-ups from heat and UV exposure."
            );
            case "Lines" -> List.of(
                    "Use sunscreen daily because UV exposure is one of the biggest visible aging triggers.",
                    "Keep hydration steady with a barrier-supportive moisturizer.",
                    "Avoid over-cleansing or overly drying routines that make lines look more obvious."
            );
            case "Hydration" -> List.of(
                    "Use a moisturizer consistently on slightly damp skin to hold water in.",
                    "Cut back on harsh cleansing or over-exfoliation if skin feels tight.",
                    "Support hydration with sunscreen in the daytime so the barrier is not constantly stressed."
            );
            default -> {
                if (score >= 65) {
                    yield List.of(
                            "Keep the routine simple and consistent for two to four weeks before judging changes.",
                            "Use sunscreen daily and avoid stacking too many actives at once."
                    );
                }
                yield List.of(
                        "Maintain a simple routine and watch for changes in lighting or irritation triggers.",
                        "Use sunscreen daily to help prevent worsening visible concerns."
                );
            }
        };
    }

    private List<ProductSuggestion> productSuggestionsFor(String concern, int score) {
        return switch (concern) {
            case "Acne" -> List.of(CLEANSER, BHA, SUNSCREEN);
            case "Pigmentation" -> List.of(SUNSCREEN, NIACINAMIDE);
            case "Uniformness", "Skin Tone" -> List.of(SUNSCREEN, NIACINAMIDE, MOISTURIZER);
            case "Pores" -> List.of(CLEANSER, BHA, NIACINAMIDE);
            case "Dark Circles" -> List.of(EYE_SERUM, MOISTURIZER, SUNSCREEN);
            case "Redness" -> List.of(MOISTURIZER, SUNSCREEN, CLEANSER);
            case "Lines" -> List.of(SUNSCREEN, MOISTURIZER);
            case "Hydration" -> List.of(MOISTURIZER, CLEANSER, SUNSCREEN);
            default -> score >= 65 ? List.of(SUNSCREEN, MOISTURIZER) : List.of(MOISTURIZER);
        };
    }

    private SkinAnalysisResult unavailableResult(String message) {
        return new SkinAnalysisResult(
                "UNAVAILABLE",
                PROVIDER,
                message,
                "Skin analysis needs Groq vision configuration.",
                List.of()
        );
    }

    private SkinAnalysisResult failedResult(String message) {
        return new SkinAnalysisResult(
                "FAILED",
                PROVIDER,
                message,
                "The rest of the analysis is still available.",
                List.of()
        );
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 800 ? body : body.substring(0, 800);
    }
}
