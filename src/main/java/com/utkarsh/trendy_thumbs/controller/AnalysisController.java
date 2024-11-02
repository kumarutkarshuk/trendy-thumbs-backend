package com.utkarsh.trendy_thumbs.controller;

import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
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


}
