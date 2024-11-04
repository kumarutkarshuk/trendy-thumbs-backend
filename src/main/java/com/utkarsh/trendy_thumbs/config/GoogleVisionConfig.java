package com.utkarsh.trendy_thumbs.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

@Configuration
public class GoogleVisionConfig {

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String googleCredentialsJson;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        // Create credentials from the JSON string
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(googleCredentialsJson.getBytes()))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

        // Create and return the ImageAnnotatorClient with the specified credentials
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return ImageAnnotatorClient.create(settings);
    }
}