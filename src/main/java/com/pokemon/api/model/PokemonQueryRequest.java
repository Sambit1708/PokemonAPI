package com.pokemon.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PokemonQueryRequest {

    @Min(value = 0, message = "Offset must be greater than or equal to 0")
    private int offset = 0;

    @Min(value = 1, message = "Limit must be greater than or equal to 1")
    @Max(value = 100, message = "Limit must be less than or equal to 100")
    private int limit = 20;
}