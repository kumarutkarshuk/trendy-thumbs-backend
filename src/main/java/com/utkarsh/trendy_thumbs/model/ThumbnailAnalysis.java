package com.utkarsh.trendy_thumbs.model;

import lombok.Data;

import java.util.List;

@Data
public class ThumbnailAnalysis {

    private String videoId;

    private List<String> dominantColors;   // List of dominant colors in hex format

    private int textWordCount;             // Word count of detected text in the thumbnail

//    private List<String> fontStyles;       // List of detected font styles or text characteristics

//    private String layoutComplexity;       // Description of layout complexity (e.g., "Simple", "Moderate", "Complex")

//    private String designTrends;           // Insights on design trends (e.g., "Red Dominant", "Minimalist")
}
