package com.utkarsh.trendy_thumbs.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ColorDetails {
    // selected colors like white, black, etc
    @NotNull
    private String color;
    // or count
    @NotNull
    private int value;
    // hex
    @NotNull
    private String fill;
}

