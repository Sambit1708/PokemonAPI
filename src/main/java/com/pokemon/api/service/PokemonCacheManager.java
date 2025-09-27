package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PokemonCacheManager {

    private final PokeApiService pokeApiService;

    @Cacheable(value = "pokemon", key = "#id")
    public Pokemon getPokemonByIdWithCache(Long id) {
        log.info("Cache miss - Fetching from PokeAPI for ID: {}", id);
        // This will properly use cache when called from other services
        return pokeApiService.getPokemonById(id);
    }

    @Cacheable(value = "pokemonList")
    public List<Pokemon> getAllPokemonWithCache() {
        log.info("Cache miss - Fetching all Pokémon from PokeAPI");
        return pokeApiService.getAllPokemon();
    }


}