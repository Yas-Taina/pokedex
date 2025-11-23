package com.example.pokedex.models

data class RegisteredPokemon(
    val id: Int,
    val pokemon_id: Int,
    val pokemon_name: String,
    val types: String,
    val ability: String,
    val moves: String,
    val user_login: String
)

data class RegisterPokemonRequest(
    val pokemon_id: Int,
    val pokemon_name: String,
    val types: String,
    val ability: String,
    val moves: String,
    val user_login: String
)

data class UpdatePokemonRequest(
    val ability: String,
    val moves: String
)

data class HomeStats(
    val total: Int,
    val topTypes: List<TypeCount>,
    val topAbilities: List<AbilityCount>
)

data class TypeCount(
    val type: String,
    val count: Int
)

data class AbilityCount(
    val ability: String,
    val count: Int
)