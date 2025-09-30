package com.pokemon.api.util;

public class CommonUtil {

    public static String capitalizeName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String determineRegion(Long pokemonId) {
        if (pokemonId <= 151) return "Kanto";
        else if (pokemonId <= 251) return "Johto";
        else if (pokemonId <= 386) return "Hoenn";
        else if (pokemonId <= 493) return "Sinnoh";
        else if (pokemonId <= 649) return "Unova";
        else if (pokemonId <= 721) return "Kalos";
        else if (pokemonId <= 809) return "Alola";
        else if (pokemonId <= 905) return "Galar";
        else return "Paldea";
    }
}
