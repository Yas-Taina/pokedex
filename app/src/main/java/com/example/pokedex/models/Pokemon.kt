package com.example.pokedex.models

data class PokemonListResponse(
    val count: Int,
    val results: List<PokemonBasic>
)

data class PokemonBasic(
    val name: String,
    val url: String
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>,
    val abilities: List<AbilitySlot>,
    val moves: List<MoveSlot>,
    val sprites: Sprites,
    val species: Species
)

data class TypeSlot(
    val type: Type
)

data class Type(
    val name: String
)

data class AbilitySlot(
    val ability: Ability
)

data class Ability(
    val name: String
)

data class MoveSlot(
    val move: Move
)

data class Move(
    val name: String
)

data class Sprites(
    val front_default: String?
)

data class Species(
    val url: String
)