package com.utkarsh.trendy_thumbs.controller;

import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalysisController {
        private final AnalysisService analysisService;

        @GetMapping("/analyze")
        public ResponseEntity<List<ThumbnailAnalysis>> analyzeTrendingThumbnails(
                @RequestParam(required = false) String category
        ){

            return analysisService.analyzeTrendingThumbnails(category);

        }



}
