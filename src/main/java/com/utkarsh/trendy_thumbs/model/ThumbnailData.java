package com.utkarsh.trendy_thumbs.model;

import com.utkarsh.trendy_thumbs.model.enums.FacialExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@Document(collection = "thumbnailData")
// must here for Spring compatibility if using @NoArgsConstructor & @Builder
@AllArgsConstructor
public class ThumbnailData {
    @Id
    private String videoId;

    private String title;

    private String channelTitle;

    private String thumbnailUrl;

    private LocalDateTime fetchedAt;

    private String videoUrl;

    // for a thumbnail
    private List<String> dominantColors;

    // for a thumbnail
    private int wordCount;

    // for a thumbnail
    private List<String> objectLabels;

    // for a thumbnail
    private List<FacialExpression> facialExpressions;

    // using embedded mongodb documents (separate collections are not created)
    // ColorData & ExpressionData need not have @Document & their @Repository interfaces
    // not using @DBRef since joining (slow) will be involved in it and redundancy is fine with nosql db
    // was also getting an error related to id
    // for all thumbnails stored redundantly
//    @DBRef
    private ColorData colorData;

    // for all thumbnails stored redundantly
//    @DBRef
    private ExpressionData expressionData;

}
