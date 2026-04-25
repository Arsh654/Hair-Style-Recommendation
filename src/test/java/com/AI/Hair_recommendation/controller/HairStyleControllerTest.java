package com.AI.Hair_recommendation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.AI.Hair_recommendation.model.FaceAnalysisResult;
import com.AI.Hair_recommendation.model.FaceShape;
import com.AI.Hair_recommendation.model.HairStyleRecommendation;
import com.AI.Hair_recommendation.model.SkinAnalysisResult;
import com.AI.Hair_recommendation.service.FaceAnalysisService;
import com.AI.Hair_recommendation.service.HairStyleRecommendationService;
import com.AI.Hair_recommendation.service.SkinAnalysisService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

class HairStyleControllerTest {

    @Test
    void analyzeEndpointClonesUploadSoSkinAnalysisStillCanReadBytes() throws Exception {
        FaceAnalysisService faceAnalysisService = Mockito.mock(FaceAnalysisService.class);
        HairStyleRecommendationService hairStyleRecommendationService = Mockito.mock(HairStyleRecommendationService.class);
        SkinAnalysisService skinAnalysisService = Mockito.mock(SkinAnalysisService.class);

        HairStyleController controller = new HairStyleController(
                faceAnalysisService,
                hairStyleRecommendationService,
                skinAnalysisService
        );

        when(faceAnalysisService.analyzeFace(any())).thenAnswer(invocation -> {
            MultipartFile multipartFile = invocation.getArgument(0);
            File tempFile = Files.createTempFile("face-test-", ".jpg").toFile();
            multipartFile.transferTo(tempFile);
            tempFile.delete();
            return new FaceAnalysisResult(FaceShape.OVAL, 10, 10, 9, 9.5, "ok");
        });

        when(hairStyleRecommendationService.getRecommendations(FaceShape.OVAL)).thenReturn(
                new HairStyleRecommendation(FaceShape.OVAL, List.of(), "reason")
        );

        when(skinAnalysisService.analyzeSkin(any())).thenAnswer(invocation -> {
            MultipartFile multipartFile = invocation.getArgument(0);
            assertThat(multipartFile.getBytes()).isEqualTo("image-bytes".getBytes());
            return new SkinAnalysisResult("UNAVAILABLE", "Groq", "summary", "notes", List.of());
        });

        ResponseEntity<?> response = controller.analyzeAndRecommend(
                new OneShotMultipartFile("photo.jpg", "image-bytes".getBytes())
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private static final class OneShotMultipartFile implements MultipartFile {
        private final String originalFilename;
        private byte[] bytes;

        private OneShotMultipartFile(String originalFilename, byte[] bytes) {
            this.originalFilename = originalFilename;
            this.bytes = bytes;
        }

        @Override
        public String getName() {
            return "image";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            if (bytes == null) {
                throw new IllegalStateException("multipart bytes already consumed");
            }
            return bytes;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), getBytes());
            bytes = null;
        }
    }
}
