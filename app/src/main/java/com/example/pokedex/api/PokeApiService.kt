package com.example.pokedex.api

import com.example.pokedex.models.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PokeApiService {
    @GET("pokemon?limit=1000")
    fun getAllPokemons(): Call<PokemonListResponse>

    @GET("pokemon/{id}")
    fun getPokemonDetail(@Path("id") id: Int): Call<PokemonDetail>

    @GET("type/{type}")
    fun getPokemonsByType(@Path("type") type: String): Call<TypePokemonResponse>

    @GET("ability/{ability}")
    fun getPokemonsByAbility(@Path("ability") ability: String): Call<AbilityPokemonResponse>

    @GET
    fun getSpeciesDetail(@Url url: String): Call<SpeciesDetail>

    @GET
    fun getEvolutionChain(@Url url: String): Call<EvolutionChainResponse>
}

data class TypePokemonResponse(
    val pokemon: List<TypePokemonSlot>
)

data class TypePokemonSlot(
    val pokemon: PokemonBasic
)

data class AbilityPokemonResponse(
    val pokemon: List<AbilityPokemonSlot>
)

data class AbilityPokemonSlot(
    val pokemon: PokemonBasic
)