package com.utkarsh.trendy_thumbs.model;

import com.utkarsh.trendy_thumbs.model.enums.FacialExpression;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// stores insights of each thumbnail extracted from Google Vision AI
public class ThumbnailAnalysis {

    private String videoId;

    private List<String> dominantColors;

    private int wordCount;

    private List<String> objectLabels;

    private List<FacialExpression> facialExpressions;

}
