package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonCacheService {

    private final PokeApiService pokeApiService;

    @Scheduled(initialDelayString = "${pokeapi.sync.initial-delay:5000}",
            fixedDelayString = "${pokeapi.sync.fixed-delay:3600000}")
    public void preloadPokemonCache() {
        log.info("Starting Pokémon cache preloading...");

        try {
            // Preload first 50 Pokémon for MVP
            for (long i = 1; i <= 50; i++) {
                try {
                    Pokemon pokemon = pokeApiService.getPokemonById(i);
                    log.debug("Preloaded Pokémon: {} - {}", pokemon.getId(), pokemon.getName());

                    // Small delay to be respectful to PokeAPI
                    Thread.sleep(100);
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
        List<Pokemon> batch = new ArrayList<>();

        for (long i = offset + 1; i <= offset + limit; i++) {
            try {
                Pokemon pokemon = pokeApiService.getPokemonById(i);
                if (pokemon != null) {
                    batch.add(pokemon);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch Pokémon ID {}: {}", i, e.getMessage());
            }
        }

        return batch;
    }
}