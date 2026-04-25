package com.AI.Hair_recommendation.service;

import com.AI.Hair_recommendation.model.FaceAnalysisResult;
import com.AI.Hair_recommendation.model.FaceShape;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FaceAnalysisService {

    private CascadeClassifier faceDetector;

    @PostConstruct
    public void init() {
        nu.pattern.OpenCV.loadLocally();
        ClassPathResource cascadeResource = new ClassPathResource("haarcascade_frontalface_default.xml");

        try (InputStream inputStream = cascadeResource.getInputStream()) {
            Path tempCascade = Files.createTempFile("haarcascade-", ".xml");
            Files.copy(inputStream, tempCascade, StandardCopyOption.REPLACE_EXISTING);
            tempCascade.toFile().deleteOnExit();

            faceDetector = new CascadeClassifier(tempCascade.toString());
            if (faceDetector.empty()) {
                faceDetector = null;
            }
        } catch (IOException exception) {
            faceDetector = null;
        }
    }

    public FaceAnalysisResult analyzeFace(MultipartFile imageFile) throws IOException {
        // Save uploaded file temporarily
        Path tempFile = Files.createTempFile("face-", imageFile.getOriginalFilename());
        imageFile.transferTo(tempFile.toFile());

        // Read image using OpenCV
        Mat image = Imgcodecs.imread(tempFile.toString());
        if (image.empty()) {
            throw new IllegalArgumentException("Uploaded file is not a readable image");
        }
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayImage, grayImage);

        // Detect faces
        Rect faceRect = detectFace(grayImage);
        if (faceRect == null) {
            throw new IllegalArgumentException("No face detected. Please upload a clearer front-facing photo.");
        }

        // Calculate proportions
        double faceWidth = faceRect.width;
        double faceLength = faceRect.height;
        double ratio = faceLength / faceWidth;

        // Estimate jaw and forehead (simplified - in reality you'd use facial landmarks)
        double jawWidth = faceWidth * 0.9;
        double foreheadWidth = faceWidth * 0.95;

        // Classify face shape based on ratio
        FaceShape shape = classifyFaceShape(ratio, faceWidth, faceLength, jawWidth, foreheadWidth);

        // Cleanup
        tempFile.toFile().delete();

        return new FaceAnalysisResult(
                shape,
                faceLength,
                faceWidth,
                jawWidth,
                foreheadWidth,
                generateAnalysisNotes(shape, ratio)
        );
    }

    private Rect detectFace(Mat grayImage) {
        if (faceDetector == null) {
            return null;
        }

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(
                grayImage,
                faceDetections,
                1.1,
                5,
                0,
                new Size(120, 120),
                new Size()
        );
        Rect[] detectedFaces = faceDetections.toArray();
        if (detectedFaces.length == 0) {
            return null;
        }

        Rect largestFace = detectedFaces[0];
        for (Rect detectedFace : detectedFaces) {
            if (detectedFace.area() > largestFace.area()) {
                largestFace = detectedFace;
            }
        }
        return largestFace;
    }

    private FaceShape classifyFaceShape(double ratio, double width, double length, double jaw, double forehead) {
        // Simplified rule-based classification
        if (ratio >= 1.35 && ratio <= 1.6 && Math.abs(forehead - jaw) <= width * 0.08) {
            return FaceShape.OVAL;
        } else if (ratio < 1.2 && jaw >= width * 0.88) {
            return FaceShape.ROUND;
        } else if (ratio >= 1.2 && ratio <= 1.4 && jaw >= width * 0.88) {
            return FaceShape.SQUARE;
        } else if (forehead > jaw * 1.1 && ratio >= 1.3) {
            return FaceShape.HEART;
        } else if (ratio > 1.5) {
            return FaceShape.OBLONG;
        } else if (forehead < jaw && ratio >= 1.3) {
            return FaceShape.DIAMOND;
        }
        return FaceShape.UNKNOWN;
    }

    private String generateAnalysisNotes(FaceShape shape, double ratio) {
        return String.format("Face shape classified as %s with length-to-width ratio of %.2f",
                shape.name(), ratio);
    }
}
