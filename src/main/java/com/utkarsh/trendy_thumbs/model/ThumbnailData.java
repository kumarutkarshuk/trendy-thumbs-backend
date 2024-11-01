package com.utkarsh.trendy_thumbs.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailData {
    @Id
    private String videoId;

    private String title;

    private String channelTitle;

    private String thumbnailUrl;

    private LocalDateTime fetchedAt;

    // Analysis fields
    @ElementCollection
    private List<String> dominantColors;   // List of dominant colors in hex format

    private int textWordCount;             // Word count of detected text in the thumbnail

//    @ElementCollection
//    private List<String> fontStyles;       // List of detected font styles or text characteristics

//    private String layoutComplexity;       // Description of layout complexity (e.g., "Simple", "Moderate", "Complex")

//    private String designTrends;           // Insights on design trends (e.g., "Red Dominant", "Minimalist")
}
