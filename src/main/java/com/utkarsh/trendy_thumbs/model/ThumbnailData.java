package com.utkarsh.trendy_thumbs.model;

import jakarta.persistence.*;
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
// must here for Spring compatibility
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
    // changing table name & PK/FK column name
    @CollectionTable(name = "dominant_colors", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "dominant_color")
    private List<String> dominantColors;

    private int wordCount;

    @ElementCollection
    @CollectionTable(name = "object_labels", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "object_label")
    private List<String> objectLabels; // what objects are there in the thumbnail

    @ElementCollection
    @CollectionTable(name = "facial_expressions", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "facial_expression")
    private List<String> facialExpressions;

}
