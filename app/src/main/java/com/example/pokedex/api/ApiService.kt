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

    @GET("pokemons/user/{user_login}")
    fun getUserPokemons(@Path("user_login") userLogin: String): Call<List<RegisteredPokemon>>

    @GET("pokemons/registered/{user_login}")
    fun getRegisteredPokemonIds(@Path("user_login") userLogin: String): Call<List<Int>>

    @PUT("pokemons/{id}")
    fun updatePokemon(
        @Path("id") id: Int,
        @Body request: UpdatePokemonRequest
    ): Call<Map<String, Any>>

    @DELETE("pokemons/{id}")
    fun deletePokemon(@Path("id") id: Int): Call<Map<String, String>>

    @GET("pokemons/details/{id}")
    fun getPokemonDetails(@Path("id") id: Int): Call<RegisteredPokemon>

    @GET("pokemons/stats/{user_login}")
    fun getHomeStats(@Path("user_login") userLogin: String): Call<HomeStats>

    @GET("pokemons/search/type/{user_login}/{type}")
    fun searchByType(
        @Path("user_login") userLogin: String,
        @Path("type") type: String
    ): Call<List<RegisteredPokemon>>

    @GET("pokemons/search/ability/{user_login}/{ability}")
    fun searchByAbility(
        @Path("user_login") userLogin: String,
        @Path("ability") ability: String
    ): Call<List<RegisteredPokemon>>
}