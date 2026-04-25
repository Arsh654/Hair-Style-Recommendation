package com.AI.Hair_recommendation.controller;

import com.AI.Hair_recommendation.model.FaceAnalysisResult;
import com.AI.Hair_recommendation.model.HairStyleRecommendation;
import com.AI.Hair_recommendation.model.SkinAnalysisResult;
import com.AI.Hair_recommendation.service.FaceAnalysisService;
import com.AI.Hair_recommendation.service.HairStyleRecommendationService;
import com.AI.Hair_recommendation.service.SkinAnalysisService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HairStyleController {

    private final FaceAnalysisService faceAnalysisService;
    private final HairStyleRecommendationService hairStyleRecommendationService;
    private final SkinAnalysisService skinAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeAndRecommend(@RequestParam("image") MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            MultipartFile faceImage = duplicateMultipartFile(image, imageBytes);
            MultipartFile skinImage = duplicateMultipartFile(image, imageBytes);

            // Analyze face
            FaceAnalysisResult analysis = faceAnalysisService.analyzeFace(faceImage);

            // Get recommendations
            HairStyleRecommendation recommendations =
                    hairStyleRecommendationService.getRecommendations(analysis.getFaceShape());
            SkinAnalysisResult skinAnalysis = skinAnalysisService.analyzeSkin(skinImage);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("faceAnalysis", analysis);
            response.put("recommendations", recommendations);
            response.put("skinAnalysis", skinAnalysis);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    private MultipartFile duplicateMultipartFile(MultipartFile original, byte[] bytes) throws IOException {
        return new InMemoryMultipartFile(
                original.getName(),
                original.getOriginalFilename(),
                original.getContentType(),
                bytes
        );
    }

    private static final class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        private InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
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
            return bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Hair Style Analyzer API is running");
    }
}
