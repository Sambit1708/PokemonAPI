package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonCacheService {

    private final PokemonCacheManager pokemonCacheManager;

    @PostConstruct
    @Scheduled(initialDelayString = "${pokeapi.sync.initial-delay:5000}",
            fixedDelayString = "${pokeapi.sync.fixed-delay:3600000}")
    public void preloadPokemonCache() {
        if (!isCachePreloadingEnabled()) {
            log.info("Cache preloading disabled");
            return;
        }

        log.info("Starting Pokémon cache preloading...");

        try {
            for (long i = 1; i <= 100; i++) {
                try {
                    // Use the cache manager instead of direct service call
                    Pokemon pokemon = pokemonCacheManager.getPokemonByIdWithCache(i);
                    log.debug("Preloaded Pokémon: {} - {}", pokemon.getId(), pokemon.getName());
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.warn("Failed to preload Pokémon ID {}: {}", i, e.getMessage());
                }
            }
            log.info("Pokémon cache preloading completed. Loaded 100 Pokémon into memory.");
        } catch (Exception e) {
            log.error("Error during cache preloading: {}", e.getMessage(), e);
        }
    }

    public List<Pokemon> getPokemonBatch(int offset, int limit) {
        return LongStream.range(offset + 1, offset + limit + 1)
                .mapToObj(i -> {
                    try {
                        return pokemonCacheManager.getPokemonByIdWithCache(i);
                    } catch (Exception e) {
                        log.warn("Failed to fetch Pokémon ID {}: {}", i, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isCachePreloadingEnabled() {
        return true;
    }
}