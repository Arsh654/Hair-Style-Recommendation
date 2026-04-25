package com.AI.Hair_recommendation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.AI.Hair_recommendation.model.FaceAnalysisResult;
import com.AI.Hair_recommendation.model.FaceShape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.mock.web.MockMultipartFile;

class FaceAnalysisServiceTest {

    private final FaceAnalysisService service = new FaceAnalysisService();

    FaceAnalysisServiceTest() {
        service.init();
    }

    @Test
    void analyzesRealPortraitPhotoInsteadOfFallingBackToUnknown() throws IOException {
        Path sampleImage = Path.of("/home/arshad/Downloads/Media.jpeg");
        byte[] imageBytes = Files.readAllBytes(sampleImage);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                sampleImage.getFileName().toString(),
                "image/jpeg",
                imageBytes
        );

        FaceAnalysisResult result = service.analyzeFace(image);

        assertThat(result.getFaceShape()).isNotEqualTo(FaceShape.UNKNOWN);
    }

    @Test
    void analyzesSecondRealPortraitPhotoInsteadOfFallingBackToUnknown() throws IOException {
        Path sampleImage = Path.of("/home/arshad/Downloads/Media (1).jpeg");
        byte[] imageBytes = Files.readAllBytes(sampleImage);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                sampleImage.getFileName().toString(),
                "image/jpeg",
                imageBytes
        );

        FaceAnalysisResult result = service.analyzeFace(image);

        assertThat(result.getFaceShape()).isNotEqualTo(FaceShape.UNKNOWN);
    }

    @Test
    void throwsHelpfulErrorWhenNoFaceIsDetected() throws IOException {
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "blank.jpg",
                "image/jpeg",
                outputStream.toByteArray()
        );

        assertThatThrownBy(() -> service.analyzeFace(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No face detected. Please upload a clearer front-facing photo.");
    }

    @Test
    void loadsCascadeClassifierBeforeAnalysis() throws Exception {
        Field detectorField = FaceAnalysisService.class.getDeclaredField("faceDetector");
        detectorField.setAccessible(true);

        CascadeClassifier detector = (CascadeClassifier) detectorField.get(service);

        assertThat(detector).isNotNull();
        assertThat(detector.empty()).isFalse();
    }

    @Test
    void detectsFaceAfterApplyingStandardPreprocessing() throws Exception {
        Field detectorField = FaceAnalysisService.class.getDeclaredField("faceDetector");
        detectorField.setAccessible(true);
        CascadeClassifier detector = (CascadeClassifier) detectorField.get(service);

        Mat image = Imgcodecs.imread("/home/arshad/Downloads/Media.jpeg");
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayImage, grayImage);

        MatOfRect faceDetections = new MatOfRect();
        detector.detectMultiScale(
                grayImage,
                faceDetections,
                1.1,
                5,
                0,
                new Size(120, 120),
                new Size()
        );

        assertThat(faceDetections.toArray()).isNotEmpty();
    }
}
