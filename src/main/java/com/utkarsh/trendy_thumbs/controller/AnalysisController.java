package com.utkarsh.trendy_thumbs.controller;

import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.dto.ColorDetails;
import com.utkarsh.trendy_thumbs.model.dto.ExpressionDetails;
import com.utkarsh.trendy_thumbs.model.dto.LastAnalyzedDateDTO;
import com.utkarsh.trendy_thumbs.model.dto.ThumbnailDTO;
import com.utkarsh.trendy_thumbs.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        public ResponseEntity<List<ThumbnailDTO>> getTrendingThumbnails(){
            return analysisService.getTrendingThumbnails();
        }

        @GetMapping("/wordCountList")
        public ResponseEntity<List<Integer>> getWordCountList(){
            return analysisService.getWordCountList();
        }

        @GetMapping("/dominantColorDetails")
        public ResponseEntity<List<ColorDetails>> getDominantColorDetails(){
            return analysisService.getDominantColorDetails();
        }

        @GetMapping("/facialExpressionDetails")
        public ResponseEntity<List<ExpressionDetails>> getFacialExpressionDetails(){
            return analysisService.getFacialExpressionDetails();
        }

        @GetMapping("/lastAnalyzedDate")
        public ResponseEntity<LastAnalyzedDateDTO> getLastAnalyzedDate(){
            return analysisService.getLastAnalyzedDate();
        }

}
