package com.utkarsh.trendy_thumbs.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExpressionCategory {
    @NotNull
    private int joy;
    @NotNull
    private int sorrow;
    @NotNull
    private int anger;
    @NotNull
    private int surprise;
    @NotNull
    private int headWear;
    @NotNull
    private int noFace;
    @NotNull
    private int other;
}
