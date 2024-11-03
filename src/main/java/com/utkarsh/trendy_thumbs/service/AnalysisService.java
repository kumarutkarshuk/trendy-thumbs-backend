package com.utkarsh.trendy_thumbs.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.utkarsh.trendy_thumbs.model.enums.FacialExpression;
import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import com.utkarsh.trendy_thumbs.model.dto.ColorCategory;
import com.utkarsh.trendy_thumbs.model.dto.ExpressionCategory;
import com.utkarsh.trendy_thumbs.repo.ThumbnailDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final YoutubeService youtubeService;

    private final GoogleVisionService googleVisionService;

    private final ThumbnailDataRepo thumbnailDataRepo;

    private final Cache<String, List<ThumbnailData>> thumbnailDataCache;

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

    public ResponseEntity<ColorCategory> getCategorizedColors() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();
            List<String> hexColors = new ArrayList<>();
            // can be improved I think
            thumbnailDataList
                    .stream()
                    .forEach(d -> d.getDominantColors().stream().forEach(color -> hexColors.add(color)));
            ColorCategory categorizedColors = categorizeColors(hexColors);
            return new ResponseEntity<>(categorizedColors, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(ColorCategory.builder().build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ExpressionCategory> getFacialExpressionsCategorized() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();

            ExpressionCategory expressionCategory = ExpressionCategory.builder().build();

            // can be improved I think
            thumbnailDataList.stream().forEach(d -> d.getFacialExpressions().stream().forEach(
                   e -> {
                       if (e == FacialExpression.JOY){
                           expressionCategory.setJoy(expressionCategory.getJoy() + 1);
                       }else if(e == FacialExpression.SORROW){
                           expressionCategory.setSorrow(expressionCategory.getSorrow() + 1);
                       }else if(e == FacialExpression.ANGER){
                           expressionCategory.setAnger(expressionCategory.getAnger() + 1);
                       }else if(e == FacialExpression.SURPRISE){
                           expressionCategory.setSurprise(expressionCategory.getSurprise() + 1);
                       }else if(e == FacialExpression.HEADWEAR){
                           expressionCategory.setHeadWear(expressionCategory.getHeadWear() + 1);
                       }else if(e == FacialExpression.NOFACE){
                           expressionCategory.setNoFace(expressionCategory.getNoFace() + 1);
                       }else{
                           expressionCategory.setOther(expressionCategory.getOther() + 1);
                       }
                   }
            ));

            return new ResponseEntity<>(expressionCategory, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(ExpressionCategory.builder().build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<Integer>> getWordCountList() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();

            List<Integer> wordCountList = thumbnailDataList.stream().map(d -> d.getWordCount()).toList();

            return new ResponseEntity<>(wordCountList, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ThumbnailAnalysis analyzeThumbnail(ThumbnailData thumbnailData) {
        try {
            return googleVisionService.analyzeThumbnail(thumbnailData);
        } catch (Exception e) {
            return ThumbnailAnalysis.builder().videoId(thumbnailData.getVideoId()).build();
        }
    }

    public List<ThumbnailData> getThumbnailData() throws Exception {
        try{
            List<ThumbnailData> data = thumbnailDataCache.getIfPresent("thumbnailData");

            if (data != null) {
//                System.out.println("Cache hit");
                return data;
            } else {
//                System.out.println("Cache miss");
                PageRequest pageRequest = PageRequest.of(0, 50, Sort.by("fetchedAt").descending());
                data = thumbnailDataRepo.findAll(pageRequest).getContent();
                thumbnailDataCache.put("thumbnailData", data);
                return data;
            }

        } catch (Exception e) {
            throw new Exception("Error fetching thumbnail data");
        }
    }

    private ColorCategory categorizeColors(List<String> hexColors) {
        ColorCategory categorizedColors = ColorCategory.builder().build();

        for (String hex : hexColors) {
            updateCategorizedColors(hex, categorizedColors);
        }

        return categorizedColors;
    }

    private void updateCategorizedColors(String hex, ColorCategory categorizedColors) {
        try {
            Color color = Color.decode(hex);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();

            if (r > 200 && g > 200 && b > 200) {
                categorizedColors.setWhite(categorizedColors.getWhite() + 1);
            } else if (r < 50 && g < 50 && b < 50) {
                categorizedColors.setBlack(categorizedColors.getBlack() + 1);
            } else if (Math.abs(r - g) < 30 && Math.abs(g - b) < 30) {
                categorizedColors.setGray(categorizedColors.getGray() + 1);
            } else if (r > g && r > b) {
                categorizedColors.setRed(categorizedColors.getRed() + 1);
            } else if (g > r && g > b) {
                categorizedColors.setGreen(categorizedColors.getGreen() + 1);
            } else if (b > r && b > g) {
                categorizedColors.setBlue(categorizedColors.getBlue() + 1);
            } else if (r > 200 && g > 200) {
                categorizedColors.setYellow(categorizedColors.getYellow() + 1);
            }else{
                categorizedColors.setOther(categorizedColors.getOther() + 1);
            }

        } catch (Exception e) {
            categorizedColors.setOther(categorizedColors.getOther() + 1);
        }
    }

}
