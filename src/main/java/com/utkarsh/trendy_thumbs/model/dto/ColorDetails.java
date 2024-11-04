package com.utkarsh.trendy_thumbs.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ColorDetails {
    @NotNull
    private String color;
    @NotNull
    private int value;
    @NotNull
    private String fill;
}

