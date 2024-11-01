package com.utkarsh.trendy_thumbs.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeService {
    private final YouTube youTube;

    @Value("${YOUTUBE_API_KEY}")
    private String apiKey;

    public List<ThumbnailData> fetchTrendingVideos(String category) throws IOException {
        YouTube.Videos.List request = youTube.videos().list("snippet,contentDetails");
        request.setKey(apiKey);
        request.setChart("mostPopular");
        request.setRegionCode("IN");

        if (category != null) {
            request.setVideoCategoryId(category);
        }

        // Limiting to 50 videos (YouTube API max)
        request.setMaxResults(50L);

        VideoListResponse response = request.execute();

        return response.getItems().stream()
                .map(this::convertToThumbnailData)
                .collect(Collectors.toList());
    }

    private ThumbnailData convertToThumbnailData(Video video) {
        return ThumbnailData
                .builder()
                .videoId(video.getId())
                .title(video.getSnippet().getTitle())
                .channelTitle(video.getSnippet().getChannelTitle())
                .thumbnailUrl(video.getSnippet().getThumbnails().getHigh().getUrl())
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}
