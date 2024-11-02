package com.utkarsh.trendy_thumbs.service;

import com.google.cloud.vision.v1.*;
import com.utkarsh.trendy_thumbs.model.FacialExpression;
import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleVisionService {

    private final ImageAnnotatorClient visionClient;

    public ThumbnailAnalysis analyzeThumbnail(ThumbnailData thumbnailData) throws IOException {
        // Load image from URL
        Image image = Image.newBuilder()
                .setSource(ImageSource.newBuilder()
                        .setImageUri(thumbnailData.getThumbnailUrl()))
                .build();

        // Perform analysis
        List<String> dominantColors = extractDominantColors(image);
        int textWordCount = countTextWords(image);
        List<String> objectLabels = detectObjectLabels(image);
        List<FacialExpression> facialExpressions = detectExpressions(image);

        // Build analysis result
        return ThumbnailAnalysis
                .builder()
                .videoId(thumbnailData.getVideoId())
                .dominantColors(dominantColors)
                .wordCount(textWordCount)
                .objectLabels(objectLabels)
                .facialExpressions(facialExpressions)
                .build();
    }

    public List<String> extractDominantColors(Image image) throws IOException {
        // Create an AnnotateImageRequest to analyze the image for its properties
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build())
                .setImage(image)
                .build();

        // Execute the request and get the response
        AnnotateImageResponse response = visionClient.batchAnnotateImages(List.of(request)).getResponsesList().get(0);

        // Extract the dominant colors from the image properties annotation
        DominantColorsAnnotation colorsAnnotation = response.getImagePropertiesAnnotation().getDominantColors();

        // Process the dominant colors, limiting to the top 5 colors
        return colorsAnnotation.getColorsList().stream()
                .limit(5)
                .map(color -> formatColor(color))
                .collect(Collectors.toList());
    }

    private String formatColor(ColorInfo colorInfo) {
        int red = (int) (colorInfo.getColor().getRed());
        int green = (int) (colorInfo.getColor().getGreen());
        int blue = (int) (colorInfo.getColor().getBlue());

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    private int countTextWords(Image image) throws IOException {
        // Create an AnnotateImageRequest to detect text in the image
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
                .setImage(image)
                .build();

        // Execute the request and get the response
        List<EntityAnnotation> textAnnotations = visionClient.batchAnnotateImages(List.of(request))
                .getResponsesList()
                .get(0)
                .getTextAnnotationsList();

        // Return the word count from the detected text
        return textAnnotations.isEmpty() ? 0 : textAnnotations.get(0).getDescription().split("\\s+").length;
    }

    public List<String> detectObjectLabels(Image image) throws IOException {
        // Create a request for LABEL_DETECTION
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build())
                .setImage(image)
                .build();

        // Perform the request
        List<AnnotateImageResponse> responses = visionClient.batchAnnotateImages(List.of(request)).getResponsesList();

        // Check for errors
        if (responses.isEmpty() || responses.get(0).hasError()) {
            System.err.println("Error detecting labels: " + (responses.isEmpty() ? "No object detected" : responses.get(0).getError().getMessage()));
            return List.of();
        }

        // Extract labels from the response (excluding confidence %)
        return responses.get(0).getLabelAnnotationsList().stream()
                .map(label -> label.getDescription())
                .collect(Collectors.toList());
    }

    public List<FacialExpression> detectExpressions(Image image) throws IOException {
        // Create a request for FACE_DETECTION
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build())
                .setImage(image)
                .build();

        // Perform the request
        List<AnnotateImageResponse> responses = visionClient.batchAnnotateImages(List.of(request)).getResponsesList();

        // Check for errors
        if (responses.isEmpty() || responses.get(0).hasError()) {
            System.err.println("Error detecting faces: " + (responses.isEmpty() ? "No face detected" : responses.get(0).getError().getMessage()));
            return List.of();
        }

        // List to hold likely expressions
        List<FacialExpression> likelyExpressions = new ArrayList<>();

        // Extract likely facial expressions from the response
        for (FaceAnnotation face : responses.get(0).getFaceAnnotationsList()) {
            if (face.getJoyLikelihood().getNumber() >= 3) {
                likelyExpressions.add(FacialExpression.JOY);
            }
            if (face.getSorrowLikelihood().getNumber() >= 3) {
                likelyExpressions.add(FacialExpression.SORROW);
            }
            if (face.getAngerLikelihood().getNumber() >= 3) {
                likelyExpressions.add(FacialExpression.ANGER);
            }
            if (face.getSurpriseLikelihood().getNumber() >= 3) {
                likelyExpressions.add(FacialExpression.SURPRISE);
            }
            if (face.getHeadwearLikelihood().getNumber() >= 3) {
                likelyExpressions.add(FacialExpression.HEADWEAR);
            }
        }

        // add unique expressions to the list (for the cases where 2 faces with same emotion are there)
        return likelyExpressions.stream().distinct().collect(Collectors.toList());
    }

}
