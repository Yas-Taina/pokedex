package com.example.pokedex.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokemonDetailsActivity : AppCompatActivity() {

    private lateinit var imgPokemon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvTypes: TextView
    private lateinit var tvAbility: TextView
    private lateinit var tvMoves: TextView
    private lateinit var layoutEvolution: LinearLayout

    private var pokemonApiId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_details)

        val pokemonDbId = intent.getIntExtra("pokemon_id", 0)

        imgPokemon = findViewById(R.id.imgPokemon)
        tvName = findViewById(R.id.tvName)
        tvTypes = findViewById(R.id.tvTypes)
        tvAbility = findViewById(R.id.tvAbility)
        tvMoves = findViewById(R.id.tvMoves)
        layoutEvolution = findViewById(R.id.layoutEvolution)

        loadPokemonDetails(pokemonDbId)
    }

    private fun loadPokemonDetails(dbId: Int) {
        RetrofitClient.apiService.getPokemonDetails(dbId)
            .enqueue(object : Callback<RegisteredPokemon> {
                override fun onResponse(
                    call: Call<RegisteredPokemon>,
                    response: Response<RegisteredPokemon>
                ) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            pokemonApiId = pokemon.pokemon_id
                            displayPokemonInfo(pokemon)
                            loadPokemonImage(pokemon.pokemon_id)
                            loadEvolutionChain(pokemon.pokemon_id)
                        }
                    }
                }

                override fun onFailure(call: Call<RegisteredPokemon>, t: Throwable) {
                    Toast.makeText(
                        this@PokemonDetailsActivity,
                        "Erro ao carregar detalhes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayPokemonInfo(pokemon: RegisteredPokemon) {
        tvName.text = "Nome: ${pokemon.pokemon_name.capitalize()}"
        tvTypes.text = "Tipos: ${pokemon.types.split(",").joinToString(", ")}"
        tvAbility.text = "Habilidade: ${pokemon.ability}"
        tvMoves.text = "Ataques: ${pokemon.moves.split(",").joinToString(", ")}"
    }

    private fun loadPokemonImage(pokemonId: Int) {
        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
        Glide.with(this)
            .load(imageUrl)
            .into(imgPokemon)
    }

    private fun loadEvolutionChain(pokemonId: Int) {
        RetrofitClient.pokeApiService.getPokemonDetail(pokemonId)
            .enqueue(object : Callback<PokemonDetail> {
                override fun onResponse(
                    call: Call<PokemonDetail>,
                    response: Response<PokemonDetail>
                ) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            loadSpeciesData(pokemon.species.url)
                        }
                    }
                }

                override fun onFailure(call: Call<PokemonDetail>, t: Throwable) {
                    Toast.makeText(
                        this@PokemonDetailsActivity,
                        "Erro ao carregar espécie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadSpeciesData(speciesUrl: String) {
        RetrofitClient.pokeApiService.getSpeciesDetail(speciesUrl)
            .enqueue(object : Callback<SpeciesDetail> {
                override fun onResponse(
                    call: Call<SpeciesDetail>,
                    response: Response<SpeciesDetail>
                ) {
                    if (response.isSuccessful) {
                        val species = response.body()
                        if (species != null) {
                            loadEvolutionChainData(species.evolution_chain.url)
                        }
                    }
                }

                override fun onFailure(call: Call<SpeciesDetail>, t: Throwable) {
                    Toast.makeText(
                        this@PokemonDetailsActivity,
                        "Erro ao carregar dados da espécie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadEvolutionChainData(chainUrl: String) {
        RetrofitClient.pokeApiService.getEvolutionChain(chainUrl)
            .enqueue(object : Callback<EvolutionChainResponse> {
                override fun onResponse(
                    call: Call<EvolutionChainResponse>,
                    response: Response<EvolutionChainResponse>
                ) {
                    if (response.isSuccessful) {
                        val chain = response.body()
                        if (chain != null) {
                            displayEvolutionChain(chain.chain)
                        }
                    }
                }

                override fun onFailure(call: Call<EvolutionChainResponse>, t: Throwable) {
                    Toast.makeText(
                        this@PokemonDetailsActivity,
                        "Erro ao carregar cadeia evolutiva",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayEvolutionChain(chain: Chain) {
        val evolutions = mutableListOf<String>()
        collectEvolutions(chain, evolutions)

        val evolutionText = "Cadeia Evolutiva: ${evolutions.joinToString(" → ")}"
        val tvEvolution = TextView(this)
        tvEvolution.text = evolutionText
        tvEvolution.textSize = 16f
        tvEvolution.setPadding(16, 16, 16, 16)
        layoutEvolution.addView(tvEvolution)
    }

    private fun collectEvolutions(chain: Chain, evolutions: MutableList<String>) {
        evolutions.add(chain.species.name.capitalize())
        for (evolution in chain.evolves_to) {
            collectEvolutions(evolution, evolutions)
        }
    }
}