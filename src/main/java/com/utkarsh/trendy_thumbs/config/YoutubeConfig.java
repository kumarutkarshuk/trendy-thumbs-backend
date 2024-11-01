package com.utkarsh.trendy_thumbs.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.services.youtube.YouTube;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;

@Configuration
public class YoutubeConfig {
    @Value("${YOUTUBE_API_KEY}")
    private String apiKey;

    @Bean
    public YouTube youTubeService() throws Exception {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                null
        )
                .setApplicationName("TrendyThumbs")
                .build();
    }
}
