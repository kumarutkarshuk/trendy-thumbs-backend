package com.utkarsh.trendy_thumbs.service;

import com.utkarsh.trendy_thumbs.model.ColorCategory;
import com.utkarsh.trendy_thumbs.model.FacialExpression;
import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import com.utkarsh.trendy_thumbs.repo.ThumbnailDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final YoutubeService youtubeService;

    private final GoogleVisionService googleVisionService;

    private final ThumbnailDataRepo thumbnailDataRepo;

    public ResponseEntity<List<ThumbnailAnalysis>> analyzeTrendingThumbnails() {

        List<ThumbnailData> thumbnails = null;
        try {
            thumbnails = youtubeService.fetchTrendingVideos();
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Analyze each thumbnail
        List<ThumbnailAnalysis> thumbnailAnalysisList = thumbnails.stream()
                .map(d -> analyzeThumbnail(d))
                .collect(Collectors.toList());

        int n = thumbnails.size();
        for(int i=0; i<n; i++) {

            ThumbnailData thumbnailData = thumbnails.get(i);
            ThumbnailAnalysis thumbnailAnalysis = thumbnailAnalysisList.get(i);

            thumbnailData.setDominantColors(thumbnailAnalysis.getDominantColors());
            thumbnailData.setWordCount(thumbnailAnalysis.getWordCount());
            thumbnailData.setObjectLabels(thumbnailAnalysis.getObjectLabels());
            thumbnailData.setFacialExpressions(thumbnailAnalysis.getFacialExpressions());
        }

        thumbnailDataRepo.saveAll(thumbnails);

        return new ResponseEntity<>(thumbnailAnalysisList, HttpStatus.OK);
    }

    public ResponseEntity<List<String>> getTrendingThumbnails() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();
            List<String> trendingThumbnails = new ArrayList<>();
            for(int i=0; i<12; i++){
                trendingThumbnails.add(thumbnailDataList.get(i).getThumbnailUrl());
            }
            return new ResponseEntity<>(trendingThumbnails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<ColorCategory, Integer>> getCategorizedColors() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();
            List<String> hexColors = new ArrayList<>();
            // can be improved I think
            thumbnailDataList
                    .stream()
                    .forEach(d -> d.getDominantColors().stream().forEach(color -> hexColors.add(color)));
            Map<ColorCategory, Integer> categorizedColors = categorizeColors(hexColors);
            return new ResponseEntity<>(categorizedColors, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<FacialExpression, Integer>> getFacialExpressionsCategorized() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();

            Map<FacialExpression, Integer> facialExpressionsCategorized = new HashMap<>();

            facialExpressionsCategorized.put(FacialExpression.JOY, 0);
            facialExpressionsCategorized.put(FacialExpression.SORROW, 0);
            facialExpressionsCategorized.put(FacialExpression.ANGER, 0);
            facialExpressionsCategorized.put(FacialExpression.SURPRISE, 0);
            facialExpressionsCategorized.put(FacialExpression.HEADWEAR, 0);

            // can be improved I think
            thumbnailDataList.stream().forEach(d -> d.getFacialExpressions().stream().forEach(
                    e -> facialExpressionsCategorized.put(e, facialExpressionsCategorized.get(e) + 1)
            ));

            return new ResponseEntity<>(facialExpressionsCategorized, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Integer>> getWordCountCategorized() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();

            Map<String, Integer> wordCountCategorized = new HashMap<>();
            wordCountCategorized.put("0-5", 0);
            wordCountCategorized.put("6-10", 0);
            wordCountCategorized.put("11-15", 0);
            wordCountCategorized.put("16-20", 0);
            wordCountCategorized.put("20+", 0);

            for(ThumbnailData thumbnailData : thumbnailDataList){
                if(thumbnailData.getWordCount() <= 5){
                    wordCountCategorized.put("0-5", wordCountCategorized.get("0-5") + 1);
                }else if(thumbnailData.getWordCount() >= 6 && thumbnailData.getWordCount() <= 10){
                    wordCountCategorized.put("6-10", wordCountCategorized.get("6-10") + 1);
                }else if(thumbnailData.getWordCount() >= 11 && thumbnailData.getWordCount() <= 15){
                    wordCountCategorized.put("11-15", wordCountCategorized.get("11-15") + 1);
                }else if(thumbnailData.getWordCount() >= 16 && thumbnailData.getWordCount() <= 20){
                    wordCountCategorized.put("16-20", wordCountCategorized.get("16-20") + 1);
                }else{
                    wordCountCategorized.put("20+", wordCountCategorized.get("20+") + 1);
                }
            }

            return new ResponseEntity<>(wordCountCategorized, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ThumbnailAnalysis analyzeThumbnail(ThumbnailData thumbnailData) {
        try {
            return googleVisionService.analyzeThumbnail(thumbnailData);
        } catch (Exception e) {
            return ThumbnailAnalysis.builder().videoId(thumbnailData.getVideoId()).build();
        }
    }

    // to be cached
    private List<ThumbnailData> getThumbnailData() throws Exception {
        try{
            PageRequest pageRequest = PageRequest.of(0, 50, Sort.by("fetchedAt").descending());
            return thumbnailDataRepo.findAll(pageRequest).getContent();
        } catch (Exception e) {
            throw new Exception("Error fetching thumbnail data");
        }
    }

    private Map<ColorCategory, Integer> categorizeColors(List<String> hexColors) {
        Map<ColorCategory, Integer> categories = new HashMap<>();

        categories.put(ColorCategory.WHITE, 0);
        categories.put(ColorCategory.BLACK, 0);
        categories.put(ColorCategory.GRAY, 0);
        categories.put(ColorCategory.RED, 0);
        categories.put(ColorCategory.GREEN, 0);
        categories.put(ColorCategory.BLUE, 0);
        categories.put(ColorCategory.YELLOW, 0);
        categories.put(ColorCategory.OTHER, 0);

        for (String hex : hexColors) {
            ColorCategory color = getBasicColor(hex);
            categories.put(color, categories.get(color) + 1);
        }

        return categories;
    }

    private ColorCategory getBasicColor(String hex) {
        try {
            Color color = Color.decode(hex);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();

            if (r > 200 && g > 200 && b > 200) return ColorCategory.WHITE;
            if (r < 50 && g < 50 && b < 50) return ColorCategory.BLACK;
            if (Math.abs(r - g) < 30 && Math.abs(g - b) < 30) return ColorCategory.GRAY;
            if (r > g && r > b) return ColorCategory.RED;
            if (g > r && g > b) return ColorCategory.GREEN;
            if (b > r && b > g) return ColorCategory.BLUE;
            if (r > 200 && g > 200) return ColorCategory.YELLOW;

            return ColorCategory.OTHER;
        } catch (Exception e) {
            return ColorCategory.OTHER;
        }
    }
}
