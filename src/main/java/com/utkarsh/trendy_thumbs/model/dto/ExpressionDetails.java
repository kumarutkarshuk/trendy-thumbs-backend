package com.utkarsh.trendy_thumbs.model.dto;

import com.utkarsh.trendy_thumbs.model.enums.FacialExpression;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExpressionDetails {
    @NotNull
    private FacialExpression expression;
    @NotNull
    private int value;
}
