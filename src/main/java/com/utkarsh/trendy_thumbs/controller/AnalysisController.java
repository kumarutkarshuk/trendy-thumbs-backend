package com.utkarsh.trendy_thumbs.controller;

import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.dto.ColorCategory;
import com.utkarsh.trendy_thumbs.model.dto.ExpressionCategory;
import com.utkarsh.trendy_thumbs.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {
        private final AnalysisService analysisService;

        @PostMapping("/analyze")
        public ResponseEntity<List<ThumbnailAnalysis>> analyzeTrendingThumbnails(){
            return analysisService.analyzeTrendingThumbnails();
        }

        @GetMapping("/thumbnails")
        public ResponseEntity<List<String>> getTrendingThumbnails(){
            return analysisService.getTrendingThumbnails();
        }

        @GetMapping("/dominantColorsCategorized")
        public ResponseEntity<ColorCategory> getCategorizedColors(){
            return analysisService.getCategorizedColors();
        }

        @GetMapping("/facialExpressionsCategorized")
        public ResponseEntity<ExpressionCategory> getFacialExpressionsCategorized(){
            return analysisService.getFacialExpressionsCategorized();
        }

        @GetMapping("/wordCountList")
        public ResponseEntity<List<Integer>> getWordCountList(){
            return analysisService.getWordCountList();
        }
}
