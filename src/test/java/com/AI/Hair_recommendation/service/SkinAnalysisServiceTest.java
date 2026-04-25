package com.AI.Hair_recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.AI.Hair_recommendation.model.SkinAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class SkinAnalysisServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void returnsUnavailableResultWhenApiKeyMissing() throws IOException {
        SkinAnalysisService service = new SkinAnalysisService(
                new RestTemplate(),
                new ObjectMapper(),
                "",
                "meta-llama/llama-4-scout-17b-16e-instruct",
                "https://api.groq.com/openai/v1/chat/completions"
        );

        SkinAnalysisResult result = service.analyzeSkin(sampleImage());

        assertThat(result.getStatus()).isEqualTo("UNAVAILABLE");
        assertThat(result.getConcerns()).isEmpty();
        assertThat(result.getSummary()).contains("GROQ_API_KEY");
    }

    @Test
    void parsesStructuredGroqResponseIntoNineConcernAssessments() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SkinAnalysisService service = new SkinAnalysisService(
                restTemplate,
                new ObjectMapper(),
                "test-key",
                "meta-llama/llama-4-scout-17b-16e-instruct",
                "https://api.groq.com/openai/v1/chat/completions"
        );

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AUTHORIZATION, "Bearer test-key"))
                .andExpect(header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(mockGroqResponse(), org.springframework.http.MediaType.APPLICATION_JSON));

        SkinAnalysisResult result = service.analyzeSkin(sampleImage());

        server.verify();
        assertThat(result.getStatus()).isEqualTo("READY");
        assertThat(result.getProvider()).isEqualTo("Groq");
        assertThat(result.getConcerns()).hasSize(9);
        assertThat(result.getConcerns().get(0).getConcern()).isEqualTo("Acne");
        assertThat(result.getConcerns().get(0).getScore()).isEqualTo(42);
        assertThat(result.getConcerns().get(0).getLabel()).isEqualTo("Balanced");
        assertThat(result.getSummary()).contains("combination");

        JsonNode acneConcern = objectMapper.valueToTree(result.getConcerns().get(0));
        assertThat(acneConcern.path("severityColor").asText()).isEqualTo("amber");
        assertThat(acneConcern.path("remedies").isArray()).isTrue();
        assertThat(acneConcern.path("remedies")).isNotEmpty();
        assertThat(acneConcern.path("productSuggestions").isArray()).isTrue();
        assertThat(acneConcern.path("productSuggestions")).isNotEmpty();

        JsonNode pigmentationConcern = objectMapper.valueToTree(
                result.getConcerns().stream()
                        .filter(concern -> "Pigmentation".equals(concern.getConcern()))
                        .findFirst()
                        .orElseThrow()
        );
        assertThat(pigmentationConcern.toString()).contains("sunscreen");
    }

    @Test
    void fallsBackToVisionModelWhenConfiguredGroqModelDoesNotSupportImages() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SkinAnalysisService service = new SkinAnalysisService(
                restTemplate,
                new ObjectMapper(),
                "test-key",
                "llama-3.3-70b-versatile",
                "https://api.groq.com/openai/v1/chat/completions"
        );

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AUTHORIZATION, "Bearer test-key"))
                .andExpect(header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("\"model\":\"meta-llama/llama-4-scout-17b-16e-instruct\"")))
                .andRespond(withSuccess(mockGroqResponse(), org.springframework.http.MediaType.APPLICATION_JSON));

        SkinAnalysisResult result = service.analyzeSkin(sampleImage());

        server.verify();
        assertThat(result.getStatus()).isEqualTo("READY");
        assertThat(result.getProvider()).isEqualTo("Groq");
    }

    @Test
    void returnsFailedResultWhenGroqRespondsWithApiError() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        SkinAnalysisService service = new SkinAnalysisService(
                restTemplate,
                new ObjectMapper(),
                "groq-test-key",
                "meta-llama/llama-4-scout-17b-16e-instruct",
                "https://api.groq.com/openai/v1/chat/completions"
        );

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AUTHORIZATION, "Bearer groq-test-key"))
                .andExpect(header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest()
                        .body("{\"error\":{\"message\":\"model does not support this request\"}}")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON));

        SkinAnalysisResult result = service.analyzeSkin(sampleImage());

        server.verify();
        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getProvider()).isEqualTo("Groq");
        assertThat(result.getSummary()).contains("configured Groq API key");
    }

    private MockMultipartFile sampleImage() {
        return new MockMultipartFile(
                "image",
                "sample.jpg",
                "image/jpeg",
                "test-image".getBytes()
        );
    }

    private String mockGroqResponse() {
        return """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"summary\\":\\"Visible skin looks generally balanced with mild combination-zone congestion and light under-eye darkness.\\",\\"concerns\\":[{\\"concern\\":\\"Acne\\",\\"score\\":42,\\"explanation\\":\\"A few small blemishes and clogged pores are visible around the T-zone.\\"},{\\"concern\\":\\"Pigmentation\\",\\"score\\":36,\\"explanation\\":\\"There are minor uneven patches, but no strong concentrated dark spots.\\"},{\\"concern\\":\\"Uniformness\\",\\"score\\":48,\\"explanation\\":\\"Tone is mostly even with some variation around the nose and cheeks.\\"},{\\"concern\\":\\"Pores\\",\\"score\\":55,\\"explanation\\":\\"Pores are moderately visible around the center of the face.\\"},{\\"concern\\":\\"Dark Circles\\",\\"score\\":51,\\"explanation\\":\\"Under-eye darkness is noticeable but not severe.\\"},{\\"concern\\":\\"Redness\\",\\"score\\":29,\\"explanation\\":\\"Only light redness is visible near the cheeks.\\"},{\\"concern\\":\\"Skin Tone\\",\\"score\\":47,\\"explanation\\":\\"Overall tone appears medium and mostly consistent across the face.\\"},{\\"concern\\":\\"Lines\\",\\"score\\":18,\\"explanation\\":\\"Few visible fine lines are present in the captured lighting.\\"},{\\"concern\\":\\"Hydration\\",\\"score\\":44,\\"explanation\\":\\"Skin does not appear very dry, though slight dullness suggests moderate hydration.\\"}]}"
                      }
                    }
                  ]
                }
                """;
    }
}
