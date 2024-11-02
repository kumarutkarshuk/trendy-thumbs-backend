package com.utkarsh.trendy_thumbs.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ColorCategory {
    @NotNull
    private int white;
    @NotNull
    private int black;
    @NotNull
    private int gray;
    @NotNull
    private int red;
    @NotNull
    private int green;
    @NotNull
    private int blue;
    @NotNull
    private int yellow;
    @NotNull
    private int other;
}
