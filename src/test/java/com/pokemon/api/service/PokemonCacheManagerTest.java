package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonCacheManagerTest {

    @Mock
    private PokeApiService pokeApiService;

    @InjectMocks
    private PokemonCacheManager pokemonCacheManager;

    private Pokemon bulbasaur;
    private Pokemon ivysaur;

    @BeforeEach
    void setup() {
        bulbasaur = new Pokemon(1L, "Bulbasaur");
        ivysaur = new Pokemon(2L, "Ivysaur");
    }

    @Test
    void getPokemonByIdWithCache_ShouldFetchFromService() {
        // given
        when(pokeApiService.getPokemonById(1L)).thenReturn(bulbasaur);

        // when
        Pokemon result = pokemonCacheManager.getPokemonByIdWithCache(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Bulbasaur");
        verify(pokeApiService, times(1)).getPokemonById(1L);
    }

    @Test
    void getAllPokemonWithCache_ShouldFetchFromService() {
        // given
        when(pokeApiService.getAllPokemon()).thenReturn(Arrays.asList(bulbasaur, ivysaur));

        // when
        List<Pokemon> result = pokemonCacheManager.getAllPokemonWithCache();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Bulbasaur", "Ivysaur");
        verify(pokeApiService, times(1)).getAllPokemon();
    }
}
