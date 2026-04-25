package com.AI.Hair_recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class StaticPageContentTest {

    @Test
    void indexPageIncludesRecommendationGalleryAndSelectedPreviewHooks() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html = resource.getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("selected-style-preview");
        assertThat(html).contains("recommendation-image");
        assertThat(html).contains("renderRecommendations");
        assertThat(html).contains("selectRecommendation(");
        assertThat(html).contains("reuploadBtn");
        assertThat(html).contains("resetSelectedFile()");
        assertThat(html).contains("skin-analysis-grid");
        assertThat(html).contains("renderSkinAnalysis");
        assertThat(html).contains("hairTabBtn");
        assertThat(html).contains("skinTabBtn");
        assertThat(html).contains("switchResultTab(");
        assertThat(html).contains("AI-powered");
        assertThat(html).contains("skin-remedies");
        assertThat(html).contains("product-suggestion-card");
        assertThat(html).contains("severityColor");
    }
}
