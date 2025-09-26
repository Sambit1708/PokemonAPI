package com.pokemon.api.model;

import lombok.Data;

import java.util.List;

@Data
public class Pokemon {
    private Long id;
    private String name;
    private List<String> types;
    private String region;
    private List<String> weaknesses;
    private PokemonSprites sprites;
    private Integer height;
    private Integer weight;
    private List<PokemonStat> stats;
}
