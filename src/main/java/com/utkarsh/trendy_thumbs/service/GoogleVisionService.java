package com.utkarsh.trendy_thumbs.service;

import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.type.Color;
import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
//        List<String> fontStyles = detectFontStyles(image);
//        int layoutComplexity = analyzeLayoutComplexity(image);
//        String designTrends = identifyDesignTrends(image);

        // Build analysis result
        ThumbnailAnalysis analysis = new ThumbnailAnalysis();
        analysis.setVideoId(thumbnailData.getVideoId());
        analysis.setDominantColors(dominantColors);
        analysis.setTextWordCount(textWordCount);
//        analysis.setFontStyles(fontStyles);
//        analysis.setLayoutComplexity(layoutComplexity);
//        analysis.setDesignTrends(designTrends);

        return analysis;
    }

    private List<String> extractDominantColors(Image image) throws IOException {
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build())
                .setImage(image)
                .build();

        AnnotateImageResponse response = visionClient.batchAnnotateImages(List.of(request)).getResponsesList().get(0);
        DominantColorsAnnotation colorsAnnotation = response.getImagePropertiesAnnotation().getDominantColors();

        return colorsAnnotation.getColorsList().stream()
                .limit(5)  // Top 5 colors
                .map(color -> formatColor(color.getColor()))
                .collect(Collectors.toList());
    }

    private int countTextWords(Image image) throws IOException {
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
                .setImage(image)
                .build();

        List<EntityAnnotation> textAnnotations = visionClient.batchAnnotateImages(List.of(request))
                .getResponsesList()
                .get(0)
                .getTextAnnotationsList();

        return textAnnotations.isEmpty() ? 0 : textAnnotations.get(0).getDescription().split("\\s+").length;
    }

    private String formatColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

//    private List<String> detectFontStyles(Image image) throws IOException {
//        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
//                .addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
//                .setImage(image)
//                .build();
//
//        List<EntityAnnotation> textAnnotations = visionClient.batchAnnotateImages(List.of(request))
//                .getResponsesList()
//                .get(0)
//                .getTextAnnotationsList();
//
//        // Detect font styles by analyzing text features (mock example)
//        return textAnnotations.stream()
//                .map(annotation -> "Bold")  // Replace with actual font style detection if available
//                .distinct()
//                .collect(Collectors.toList());
//    }

//    private int analyzeLayoutComplexity(Image image) throws IOException {
//        // Example logic for layout complexity
//        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
//                .addFeatures(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build())
//                .setImage(image)
//                .build();
//
//        List<LocalizedObjectAnnotation> objects = visionClient.batchAnnotateImages(List.of(request))
//                .getResponsesList()
//                .get(0)
//                .getLocalizedObjectAnnotationsList();
//
//        return objects.size();  // Number of objects as a proxy for layout complexity
//    }

//    private String identifyDesignTrends(Image image) throws IOException {
//        // This could include detecting if specific elements or color patterns are present
//        // Example: Check for common design elements (mock implementation)
//        List<String> dominantColors = extractDominantColors(image);
//        return dominantColors.contains("#FF0000") ? "Red Dominant" : "No Trend";
//    }
}
