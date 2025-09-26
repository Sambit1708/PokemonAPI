package com.pokemon.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokeApiResponse {
    private Long id;
    private String name;
    private List<PokemonType> types;
    private Sprites sprites;
    private Integer height;
    private Integer weight;
    private List<Stat> stats;

    @Data
    public static class PokemonType {
        private Type type;
        private Integer slot;

        @Data
        public static class Type {
            private String name;
        }
    }

    @Data
    public static class Sprites {
        @JsonProperty("front_default")
        private String frontDefault;
        @JsonProperty("back_default")
        private String backDefault;
        private Other other;

        @Data
        public static class Other {
            @JsonProperty("official-artwork")
            private OfficialArtwork officialArtwork;

            @Data
            public static class OfficialArtwork {
                @JsonProperty("front_default")
                private String frontDefault;
            }
        }
    }

    @Data
    public static class Stat {
        private Integer baseStat;
        private StatInfo stat;

        @Data
        public static class StatInfo {
            private String name;
        }
    }
}