package com.utkarsh.trendy_thumbs.service;

import com.utkarsh.trendy_thumbs.model.ThumbnailAnalysis;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import com.utkarsh.trendy_thumbs.repo.ThumbnailDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private ThumbnailAnalysis analyzeThumbnail(ThumbnailData thumbnailData) {
        try {
            return googleVisionService.analyzeThumbnail(thumbnailData);
        } catch (Exception e) {
            return ThumbnailAnalysis.builder().videoId(thumbnailData.getVideoId()).build();
        }
    }


}
