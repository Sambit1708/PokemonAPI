package com.pokemon.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class DamageRelations {

    private List<TypeInfo> double_damage_from;
    private List<TypeInfo> half_damage_from;
    private List<TypeInfo> no_damage_from;
}
