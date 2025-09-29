package com.pokemon.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public Pokemon(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
