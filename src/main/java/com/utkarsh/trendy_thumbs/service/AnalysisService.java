package com.utkarsh.trendy_thumbs.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.utkarsh.trendy_thumbs.model.ColorData;
import com.utkarsh.trendy_thumbs.model.ExpressionData;
import com.utkarsh.trendy_thumbs.model.dto.LastAnalyzedDateDTO;
import com.utkarsh.trendy_thumbs.model.dto.ThumbnailDTO;
import com.utkarsh.trendy_thumbs.model.enums.FacialExpression;
import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import com.utkarsh.trendy_thumbs.model.dto.ColorDetails;
import com.utkarsh.trendy_thumbs.model.dto.ExpressionDetails;
import com.utkarsh.trendy_thumbs.repo.ThumbnailDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            // fetch thumbnail data from YouTube
            thumbnails = youtubeService.fetchTrendingVideos();
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Analyze each thumbnail
        List<ThumbnailAnalysis> thumbnailAnalysisList = thumbnails.stream()
                .map(d -> analyzeThumbnail(d))
                .collect(Collectors.toList());

        ColorData colorData = analyzeDominantColors(thumbnailAnalysisList);

        ExpressionData expressionData = analyzeFacialExpressions(thumbnailAnalysisList);

        // save the analysed data in the db
        int n = thumbnails.size();
        for(int i=0; i<n; i++) {

            ThumbnailData thumbnailData = thumbnails.get(i);
            ThumbnailAnalysis thumbnailAnalysis = thumbnailAnalysisList.get(i);

            thumbnailData.setDominantColors(thumbnailAnalysis.getDominantColors());
            thumbnailData.setWordCount(thumbnailAnalysis.getWordCount());
            thumbnailData.setObjectLabels(thumbnailAnalysis.getObjectLabels());
            thumbnailData.setFacialExpressions(thumbnailAnalysis.getFacialExpressions());
            thumbnailData.setColorData(colorData);
            thumbnailData.setExpressionData(expressionData);
        }

        thumbnailDataRepo.saveAll(thumbnails);

        return new ResponseEntity<>(thumbnailAnalysisList, HttpStatus.OK);
    }

    public ResponseEntity<List<ThumbnailDTO>> getTrendingThumbnails() {
        try{
            List<ThumbnailData> thumbnailDataList = getThumbnailData();
            List<ThumbnailDTO> trendingThumbnails = new ArrayList<>();
            for(int i=0; i<10; i++){
                trendingThumbnails.add(ThumbnailDTO
                        .builder()
                                .thumbnailUrl(thumbnailDataList.get(i).getThumbnailUrl())
                                .title(thumbnailDataList.get(i).getTitle())
                                .videoUrl(thumbnailDataList.get(i).getVideoUrl())
                        .build());
            }
            return new ResponseEntity<>(trendingThumbnails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    public ResponseEntity<List<ColorDetails>> getDominantColorDetails() {
        try{
            ThumbnailData firstThumbnailData = getThumbnailData().get(0);
            List<ColorDetails> response = firstThumbnailData.getColorData().getColorDetailsList();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<ExpressionDetails>> getFacialExpressionDetails() {
        try{
            ThumbnailData firstThumbnailData = getThumbnailData().get(0);
            List<ExpressionDetails> response = firstThumbnailData.getExpressionData().getExpressionDetailsList();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<LastAnalyzedDateDTO> getLastAnalyzedDate() {
        try{
            ThumbnailData firstThumbnailData = getThumbnailData().get(0);
            // other analysis fields can also be used (except wordCount)
            LocalDateTime lastAnalyzedDate = firstThumbnailData.getColorData().getAnalyzedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            String formattedDate = lastAnalyzedDate.format(formatter);

            return new ResponseEntity<>(LastAnalyzedDateDTO.builder().lastAnalyzedDate(formattedDate).build(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(LastAnalyzedDateDTO.builder().build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ThumbnailAnalysis analyzeThumbnail(ThumbnailData thumbnailData) {
        try {
            return googleVisionService.analyzeThumbnail(thumbnailData);
        } catch (Exception e) {
            return ThumbnailAnalysis.builder().videoId(thumbnailData.getVideoId()).build();
        }
    }

    // maybe for caching this method has to be public
    // returns data from the cache or fetches from db
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

    private ColorData analyzeDominantColors(List<ThumbnailAnalysis> thumbnailAnalysisList) {
        List<String> hexColors = new ArrayList<>();
        
        // flatten (2D list to 1D) this dominant colors list
        thumbnailAnalysisList.forEach(d -> hexColors.addAll(d.getDominantColors()));

        // Get color data with categorized colors
        return getColorData(hexColors);
    }

    private ColorData getColorData(List<String> hexColors) {
        Map<String, Integer> colorCategoryCounts = new HashMap<>();

        for (String hex : hexColors) {
            String category = categorizeColor(hex);
            colorCategoryCounts.put(category, colorCategoryCounts.getOrDefault(category, 0) + 1);
        }

        List<ColorDetails> colorDetailsList = colorCategoryCounts.entrySet().stream()
                .map(entry -> ColorDetails
                        .builder()
                        .color(entry.getKey())
                        .value(entry.getValue())
                        .fill(getFillColor(entry.getKey()))
                        .build())
                .collect(Collectors.toList());

        return ColorData.builder().colorDetailsList(colorDetailsList).analyzedAt(LocalDateTime.now()).build();
    }

    private String categorizeColor(String hex) {
        try {
            Color color = Color.decode(hex);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();

            if (r > 200 && g > 200 && b > 200) return "White";
            else if (r < 50 && g < 50 && b < 50) return "Black";
            else if (Math.abs(r - g) < 30 && Math.abs(g - b) < 30) return "Gray";
            else if (r > g && r > b) return "Red";
            else if (g > r && g > b) return "Green";
            else if (b > r && b > g) return "Blue";
            else if (r > 200 && g > 200) return "Yellow";
            else return "Other";

        } catch (Exception e) {
            return "Other";
        }
    }

    private String getFillColor(String category) {
        switch (category) {
            case "White": return "#FFFFFF";
            case "Black": return "#000000";
            case "Gray": return "#808080";
            case "Red": return "#FF0000";
            case "Green": return "#00FF00";
            case "Blue": return "#0000FF";
            case "Yellow": return "#FFFF00";
            default: return "#A9A9A9"; // Default fill for "Other"
        }
    }

    private ExpressionData analyzeFacialExpressions(List<ThumbnailAnalysis> thumbnailAnalysisList) {

        // Map to count occurrences of each facial expression
        Map<FacialExpression, Integer> expressionCountMap = new EnumMap<>(FacialExpression.class);

        // Initialize counts to zero for all expressions
        for (FacialExpression expression : FacialExpression.values()) {
            expressionCountMap.put(expression, 0);
        }

        // Populate the count map with occurrences after flatting the list of facial expressions
        thumbnailAnalysisList.stream()
                .flatMap(thumbnail -> thumbnail.getFacialExpressions().stream())
                .forEach(expression -> expressionCountMap.put(expression, expressionCountMap.get(expression) + 1));

        // Convert count map to a list of ExpressionDetails
        List<ExpressionDetails> expressionDetailsList = expressionCountMap.entrySet().stream()
                .map(entry -> ExpressionDetails.builder()
                        .expression(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // Build and return ExpressionData
        return ExpressionData.builder()
                .expressionDetailsList(expressionDetailsList)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

}
