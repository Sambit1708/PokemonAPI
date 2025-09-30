package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PokemonCacheService {

    @Value("${pokeapi.sync.enabled:true}")
    private boolean isSyncEnabled;

    private final PokemonCacheManager pokemonCacheManager;

    // Add this missing method
    public Optional<Pokemon> getPokemonById(Long id) {
        try {
            Pokemon pokemon = pokemonCacheManager.getPokemonByIdWithCache(id);
            return Optional.ofNullable(pokemon);
        } catch (Exception e) {
            log.warn("Failed to fetch Pokémon ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    // Add this missing async method
    @Async
    public void preloadPokemonCacheAsync() {
        preloadPokemonCache();
    }

    @Async
    @Scheduled(initialDelayString = "${pokeapi.sync.initial-delay:5000}",
            fixedDelayString = "${pokeapi.sync.fixed-delay:3600000}")
    public void preloadPokemonCache() {
        if (!isSyncEnabled) {
            log.info("Cache preloading disabled");
            return;
        }

        log.info("Starting Pokémon cache preloading...");

        try {
            for (int i = 1; i <= 100; i++) {
                try {
                    Pokemon pokemon = pokemonCacheManager.getPokemonByIdWithCache((long) i);
                    log.debug("Preloaded Pokémon: {} - {}", pokemon.getId(), pokemon.getName());
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.warn("Failed to preload Pokémon ID {}: {}", i, e.getMessage());
                }
            }
            log.info("Pokémon cache preloading completed");
        } catch (Exception e) {
            log.error("Error during cache preloading: {}", e.getMessage(), e);
        }
    }

    public List<Pokemon> getPokemonBatch(int offset, int limit) {
        List<Pokemon> result = new ArrayList<>();
        for (int i = offset + 1; i <= offset + limit; i++) {
            try {
                Pokemon pokemon = pokemonCacheManager.getPokemonByIdWithCache((long) i);
                if (pokemon != null) {
                    result.add(pokemon);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch Pokémon ID {}: {}", i, e.getMessage());
            }
        }
        return result;
    }
}