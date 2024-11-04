package com.utkarsh.trendy_thumbs.model;

import com.utkarsh.trendy_thumbs.model.dto.ColorDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorData {
    private List<ColorDetails> colorDetailsList;

    private LocalDateTime analyzedAt;
}