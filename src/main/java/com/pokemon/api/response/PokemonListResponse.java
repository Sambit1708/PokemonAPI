package com.pokemon.api.response;

import com.pokemon.api.dto.PokemonRef;
import lombok.Data;

import java.util.List;

@Data
public class PokemonListResponse {

    private List<PokemonRef> results;
}