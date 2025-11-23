package com.example.pokedex.models

data class SpeciesDetail(
    val evolution_chain: EvolutionChainUrl
)

data class EvolutionChainUrl(
    val url: String
)

data class EvolutionChainResponse(
    val chain: Chain
)

data class Chain(
    val species: SpeciesInfo,
    val evolves_to: List<Chain>
)

data class SpeciesInfo(
    val name: String
)