package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import com.pokemon.api.model.PokemonSprites;
import com.pokemon.api.model.PokemonStat;
import com.pokemon.api.response.PokeApiResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PokeApiService {

    private final WebClient webClient;

    @Value("${pokeapi.max-pokemon:1025}")
    private int maxPokemon;

    public PokeApiService(WebClient.Builder webClientBuilder,
                          @Value("${pokeapi.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Cacheable(value = "pokemon", key = "#id")
    public Pokemon getPokemonById(Long id) {
        log.info("Fetching Pokémon data from PokeAPI for ID: {}", id);

        PokeApiResponse response = webClient.get()
                .uri("/pokemon/{id}", id)
                .retrieve()
                .bodyToMono(PokeApiResponse.class)
                .block();

        return mapToPokemon(response);
    }

    @Cacheable(value = "pokemonList")
    public List<Pokemon> getAllPokemon() {
        log.info("Fetching all Pokémon data from PokeAPI");

        // First, get the list of all Pokémon with basic info
        PokemonListResponse listResponse = webClient.get()
                .uri("/pokemon?limit={limit}", maxPokemon)
                .retrieve()
                .bodyToMono(PokemonListResponse.class)
                .block();

        if (listResponse == null || listResponse.getResults() == null) {
            return List.of();
        }

        // Fetch detailed information for each Pokémon concurrently
        List<Mono<Pokemon>> pokemonMonos = listResponse.getResults().stream()
                .map(pokemonRef -> {
                    // Extract Pokémon ID from URL (e.g., "https://pokeapi.co/api/v2/pokemon/1/")
                    String[] urlParts = pokemonRef.getUrl().split("/");
                    Long pokemonId = Long.parseLong(urlParts[urlParts.length - 1]);

                    return webClient.get()
                            .uri("/pokemon/{id}", pokemonId)
                            .retrieve()
                            .bodyToMono(PokeApiResponse.class)
                            .map(this::mapToPokemon)
                            .onErrorResume(e -> {
                                log.warn("Failed to fetch Pokémon ID {}: {}", pokemonId, e.getMessage());
                                return Mono.empty(); // Skip failed requests
                            });
                })
                .toList();

        // Wait for all requests to complete
        return Flux.merge(pokemonMonos)
                .collectList()
                .block();
    }

    private Pokemon mapToPokemon(PokeApiResponse response) {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(response.getId());
        pokemon.setName(capitalizeName(response.getName()));
        pokemon.setTypes(response.getTypes().stream()
                .map(type -> type.getType().getName())
                .toList());
        pokemon.setHeight(response.getHeight());
        pokemon.setWeight(response.getWeight());

        // Map sprites
        PokemonSprites sprites = new PokemonSprites();
        sprites.setFrontDefault(response.getSprites().getFrontDefault());
        sprites.setBackDefault(response.getSprites().getBackDefault());
        sprites.setOfficialArtwork(response.getSprites().getOther().getOfficialArtwork().getFrontDefault());
        pokemon.setSprites(sprites);

        // Map stats
        pokemon.setStats(response.getStats().stream()
                .map(stat -> {
                    PokemonStat pokemonStat = new PokemonStat();
                    pokemonStat.setName(stat.getStat().getName());
                    pokemonStat.setBaseStat(stat.getBaseStat());
                    return pokemonStat;
                })
                .toList());

        // Set region based on Pokémon ID ranges
        pokemon.setRegion(determineRegion(response.getId()));

        // Calculate weaknesses (simplified for MVP)
        pokemon.setWeaknesses(calculateWeaknesses(response.getTypes()));

        return pokemon;
    }

    private String capitalizeName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String determineRegion(Long pokemonId) {
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

    private List<String> calculateWeaknesses(List<PokeApiResponse.PokemonType> types) {
        // Simplified weakness calculation - in real app, use type effectiveness matrix
        return List.of("Normal", "Fighting", "Flying", "Poison", "Ground", "Rock",
                "Bug", "Ghost", "Steel", "Fire", "Water", "Grass", "Electric",
                "Psychic", "Ice", "Dragon", "Dark", "Fairy");
    }

    @Data
    private static class PokemonListResponse {
        private List<NamedAPIResource> results;

        @Data
        public static class NamedAPIResource {
            private String name;
            private String url;
        }
    }
}