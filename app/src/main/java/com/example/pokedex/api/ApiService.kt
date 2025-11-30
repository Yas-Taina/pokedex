package com.example.pokedex.api

import com.example.pokedex.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("users/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("users/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("pokemons/register")
    fun registerPokemon(@Body request: RegisterPokemonRequest): Call<Map<String, Any>>

    @GET("pokemons/register/all")
    fun getAllPokemons(): Call<List<RegisteredPokemon>>

    @GET("pokemons/user/{user_login}")
    fun getUserPokemons(@Path("user_login") userLogin: String): Call<List<RegisteredPokemon>>

    @GET("pokemons/registered/all")
    fun getRegisteredPokemonIds(): Call<List<Int>>

    @GET("pokemons/stats")
    fun getHomeStats(): Call<HomeStats>

    @GET("pokemons/search/type/{type}")
    fun searchByType(
        @Path("type") type: String
    ): Call<List<RegisteredPokemon>>

    @GET("pokemons/search/ability/{ability}")
    fun searchByAbility(
        @Path("ability") ability: String
    ): Call<List<RegisteredPokemon>>

    @GET("pokemons/details/{id}")
    fun getPokemonDetails(@Path("id") id: Int): Call<RegisteredPokemon>

    @PUT("pokemons/{id}")
    fun updatePokemon(
        @Path("id") id: Int,
        @Body request: UpdatePokemonRequest
    ): Call<Map<String, Any>>

    @DELETE("pokemons/{id}")
    fun deletePokemon(@Path("id") id: Int): Call<Map<String, String>>
}