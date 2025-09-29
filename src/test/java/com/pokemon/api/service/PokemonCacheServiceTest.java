package com.pokemon.api.service;

import com.pokemon.api.model.Pokemon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonCacheServiceTest {

    @Mock
    private PokemonCacheManager pokemonCacheManager;

    @InjectMocks
    private PokemonCacheService pokemonCacheService;


    @Test
    void preloadPokemonCache_ShouldNotRun_WhenSyncDisabled() {
        ReflectionTestUtils.setField(pokemonCacheService, "isSyncEnabled", false);

        pokemonCacheService.preloadPokemonCache();

        verifyNoInteractions(pokemonCacheManager);
    }

    @Test
    void preloadPokemonCache_ShouldPreload100Pokemons_WhenSyncEnabled() throws Exception {
        // given
        ReflectionTestUtils.setField(pokemonCacheService, "isSyncEnabled", true);

        when(pokemonCacheManager.getPokemonByIdWithCache(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return new Pokemon(id, "Pokemon-" + id);
                });

        pokemonCacheService.preloadPokemonCache();

        verify(pokemonCacheManager, times(100)).getPokemonByIdWithCache(anyLong());
    }

    @Test
    void getPokemonBatch_ShouldReturnPokemons_WhenCacheWorks() throws Exception {
        when(pokemonCacheManager.getPokemonByIdWithCache(1L))
                .thenReturn(new Pokemon(1L, "Bulbasaur"));
        when(pokemonCacheManager.getPokemonByIdWithCache(2L))
                .thenReturn(new Pokemon(2L, "Ivysaur"));

        List<Pokemon> result = pokemonCacheService.getPokemonBatch(0, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Bulbasaur");
        assertThat(result.get(1).getName()).isEqualTo("Ivysaur");
    }

    @Test
    void getPokemonBatch_ShouldSkipNulls_WhenExceptionThrown() throws Exception {
        when(pokemonCacheManager.getPokemonByIdWithCache(1L))
                .thenThrow(new RuntimeException("API error"));
        when(pokemonCacheManager.getPokemonByIdWithCache(2L))
                .thenReturn(new Pokemon(2L, "Ivysaur"));

        List<Pokemon> result = pokemonCacheService.getPokemonBatch(0, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ivysaur");
    }
}
