package com.utkarsh.trendy_thumbs.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LastAnalyzedDateDTO {
    @NotNull
    private String lastAnalyzedDate;
}
