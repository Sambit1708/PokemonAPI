package com.pokemon.api.controller;

import com.pokemon.api.exception.PokemonNotFoundException;
import com.pokemon.api.model.Pokemon;
import com.pokemon.api.model.PokemonQueryRequest;
import com.pokemon.api.response.Response;
import com.pokemon.api.service.PokemonCacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/api/v1/pokemon")
@RequiredArgsConstructor
public class PokemonController {

    private final PokemonCacheService cacheService;

    @GetMapping
    public ResponseEntity<Response> getAllPokemon(@Valid PokemonQueryRequest queryRequest) {
        try {
            List<Pokemon> pokemonBatch = cacheService.getPokemonBatch(
                    queryRequest.getOffset(),
                    queryRequest.getLimit()
            );
            if(pokemonBatch.isEmpty()) {
                throw new PokemonNotFoundException("No Data found.");
            }

            return ResponseEntity.ok(Response.success(pokemonBatch, "Data fetched successfully."));

        } catch (PokemonNotFoundException e) {
            log.error("Pokemon data not found: [getAllPokemon]");
            throw e;
        } catch (Exception e) {
            log.error("Error fetching Pokémon batch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch Pokémon data"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPokemonById(@PathVariable Long id) {
        try {
            // Add direct single Pokemon fetch method
            Pokemon pokemon = cacheService.getPokemonById(id)
                    .orElseThrow(() -> new PokemonNotFoundException("Pokemon not found with ID: " + id));

            return ResponseEntity.ok(Response.success(pokemon, "Data fetched successfully."));

        } catch (PokemonNotFoundException e) {
            log.warn("Pokemon not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching Pokémon by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to fetch Pokémon data"));
        }
    }

    @PostMapping("/cache/preload")
    public ResponseEntity<Response> triggerCachePreload() {
        try {
            cacheService.preloadPokemonCacheAsync();
            return ResponseEntity.accepted().body(
                    Response.success(Collections.emptyList(), "Cache preload started successfully.")
            );
        } catch (Exception e) {
            log.error("Error [PokemonController] [triggerCachePreload]: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to start cache preload"));
        }
    }
}