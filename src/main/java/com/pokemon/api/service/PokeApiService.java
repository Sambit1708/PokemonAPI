package com.pokemon.api.service;

import com.pokemon.api.dto.TypeResponse;
import com.pokemon.api.model.Pokemon;
import com.pokemon.api.model.PokemonSprites;
import com.pokemon.api.model.PokemonStat;
import com.pokemon.api.response.PokeApiResponse;
import com.pokemon.api.response.PokemonListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.pokemon.api.util.CommonUtil.capitalizeName;
import static com.pokemon.api.util.CommonUtil.determineRegion;

@Slf4j
@Service
public class PokeApiService {

    private final RestTemplate restTemplate;

    @Value("${pokeapi.base-url:https://pokeapi.co/api/v2}")
    private String baseUrl;

    public PokeApiService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "pokemon", key = "#id")
    public Pokemon getPokemonById(Long id) {
        log.info("Fetching Pokémon from PokeAPI for ID: {}", id);

        String url = baseUrl + "/pokemon/" + id;
        PokeApiResponse response = restTemplate.getForObject(url, PokeApiResponse.class);

        return mapToPokemon(response);
    }

    @Cacheable(value = "allPokemon")
    public List<Pokemon> getAllPokemon() {
        log.info("Fetching all Pokémon from PokeAPI");

        // First get the list
        String listUrl = baseUrl + "/pokemon?limit=100";
        PokemonListResponse listResponse = restTemplate.getForObject(listUrl, PokemonListResponse.class);

        if (listResponse == null || listResponse.getResults() == null) {
            return List.of();
        }

        // Fetch details for first 20 Pokémon (for demo)
        return listResponse.getResults().stream()
                .limit(100)
                .map(pokemonRef -> {
                    try {
                        String[] urlParts = pokemonRef.getUrl().split("/");
                        Long pokemonId = Long.parseLong(urlParts[urlParts.length - 1]);
                        return getPokemonById(pokemonId);
                    } catch (Exception e) {
                        log.warn("Failed to fetch Pokémon: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Pokemon mapToPokemon(PokeApiResponse response) {
        if (response == null) return null;

        Pokemon pokemon = new Pokemon();
        pokemon.setId(response.getId());
        pokemon.setName(capitalizeName(response.getName()));
        pokemon.setTypes(response.getTypes().stream()
                .map(type -> type.getType().getName())
                .toList());
        pokemon.setHeight(response.getHeight());
        pokemon.setWeight(response.getWeight());
        pokemon.setWeaknesses(findWeaknesses(pokemon.getTypes()));

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

        // Set region
        pokemon.setRegion(determineRegion(response.getId()));

        return pokemon;
    }

    private List<String> findWeaknesses(List<String> types) {
        Map<String, Double> multiplierMap = new HashMap<>();

        for (String type : types) {
            String typeUrl = baseUrl + "/type/" + type;
            TypeResponse typeResponse = restTemplate.getForObject(typeUrl, TypeResponse.class);

            if (typeResponse == null || typeResponse.getDamage_relations() == null) continue;

            // Double damage → ×2
            typeResponse.getDamage_relations().getDouble_damage_from()
                    .forEach(t -> multiplierMap.merge(t.getName(), 2.0, (a, b) -> a * b));

            // Half damage → ×0.5
            typeResponse.getDamage_relations().getHalf_damage_from()
                    .forEach(t -> multiplierMap.merge(t.getName(), 0.5, (a, b) -> a * b));

            // No damage → ×0
            typeResponse.getDamage_relations().getNo_damage_from()
                    .forEach(t -> multiplierMap.put(t.getName(), 0.0));
        }

        // Only keep weaknesses (multiplier > 1)
        return multiplierMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1.0)
                .map(Map.Entry::getKey)
                .toList();
    }

}