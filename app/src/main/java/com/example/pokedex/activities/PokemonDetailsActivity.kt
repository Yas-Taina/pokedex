package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokemonDetailsActivity : BaseActivity() {

    private lateinit var imgPokemon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvTypes: TextView
    private lateinit var tvAbility: TextView
    private lateinit var tvMoves: TextView
    private lateinit var layoutEvolution: LinearLayout
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private var pokemonDbId = 0
    private var currentPokemon: RegisteredPokemon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_details)
        pokemonDbId = intent.getIntExtra("pokemon_id", 0)
        initViews()
        if (pokemonDbId != 0) {
            loadPokemonDetails(pokemonDbId)
        } else {
            showAlert("Erro", "ID do Pokémon inválido.", true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (pokemonDbId != 0) loadPokemonDetails(pokemonDbId)
    }

    private fun initViews() {
        imgPokemon = findViewById(R.id.imgPokemon)
        tvName = findViewById(R.id.tvName)
        tvTypes = findViewById(R.id.tvTypes)
        tvAbility = findViewById(R.id.tvAbility)
        tvMoves = findViewById(R.id.tvMoves)
        layoutEvolution = findViewById(R.id.layoutEvolution)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun loadPokemonDetails(dbId: Int) {
        RetrofitClient.apiService.getPokemonDetails(dbId)
            .enqueue(object : Callback<RegisteredPokemon> {
                override fun onResponse(call: Call<RegisteredPokemon>, response: Response<RegisteredPokemon>) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            currentPokemon = pokemon
                            displayPokemonInfo(pokemon)

                            layoutEvolution.removeAllViews()
                            loadEvolutionChain(pokemon.pokemon_id)
                            setupButtons(pokemon)
                        }
                    }
                }
                override fun onFailure(call: Call<RegisteredPokemon>, t: Throwable) {
                    showAlert("Erro", "Falha de conexão ao carregar detalhes.")
                }
            })
    }

    private fun displayPokemonInfo(pokemon: RegisteredPokemon) {
        tvName.text = pokemon.pokemon_name.replaceFirstChar { it.uppercase() }
        tvTypes.text = pokemon.types.split(",").joinToString(", ") { it.trim().uppercase() }
        tvAbility.text = pokemon.ability.uppercase()
        tvMoves.text = pokemon.moves.split(",").joinToString("\n") { "● ${it.trim()}" }

        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.pokemon_id}.png"
        Glide.with(this).load(imageUrl).into(imgPokemon)
    }

    private fun setupButtons(pokemon: RegisteredPokemon) {
        btnEdit.visibility = View.VISIBLE
        btnDelete.visibility = View.VISIBLE

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditPokemonActivity::class.java)
            intent.putExtra("pokemon_id", pokemon.id)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            confirmDelete(pokemon)
        }
    }

    private fun confirmDelete(pokemon: RegisteredPokemon) {
        AlertDialog.Builder(this)
            .setTitle("Soltar Pokémon?")
            .setMessage("Deseja realmente excluir ${pokemon.pokemon_name} da base de dados global?")
            .setPositiveButton("Sim, Soltar") { _, _ ->
                deletePokemon(pokemon.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletePokemon(id: Int) {
        RetrofitClient.apiService.deletePokemon(id)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        showAlert("Sucesso", "Pokémon solto na natureza com sucesso!", true)
                    } else {
                        showAlert("Erro", "Não foi possível excluir o Pokémon.")
                    }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    showAlert("Erro", "Falha de conexão com o servidor.")
                }
            })
    }

    private fun loadEvolutionChain(pokemonId: Int) {
        RetrofitClient.pokeApiService.getPokemonDetail(pokemonId)
            .enqueue(object : Callback<PokemonDetail> {
                override fun onResponse(call: Call<PokemonDetail>, response: Response<PokemonDetail>) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            loadSpeciesData(pokemon.species.url)
                        }
                    }
                }
                override fun onFailure(call: Call<PokemonDetail>, t: Throwable) {}
            })
    }

    private fun loadSpeciesData(speciesUrl: String) {
        RetrofitClient.pokeApiService.getSpeciesDetail(speciesUrl)
            .enqueue(object : Callback<SpeciesDetail> {
                override fun onResponse(call: Call<SpeciesDetail>, response: Response<SpeciesDetail>) {
                    if (response.isSuccessful) {
                        val species = response.body()
                        if (species != null) {
                            loadEvolutionChainData(species.evolution_chain.url)
                        }
                    }
                }
                override fun onFailure(call: Call<SpeciesDetail>, t: Throwable) {}
            })
    }

    private fun loadEvolutionChainData(chainUrl: String) {
        RetrofitClient.pokeApiService.getEvolutionChain(chainUrl)
            .enqueue(object : Callback<EvolutionChainResponse> {
                override fun onResponse(call: Call<EvolutionChainResponse>, response: Response<EvolutionChainResponse>) {
                    if (response.isSuccessful) {
                        val chain = response.body()
                        if (chain != null) {
                            displayEvolutionChain(chain.chain)
                        }
                    }
                }
                override fun onFailure(call: Call<EvolutionChainResponse>, t: Throwable) {}
            })
    }

    private fun displayEvolutionChain(chain: Chain) {
        val evolutionList = mutableListOf<SpeciesInfo>()
        collectEvolutions(chain, evolutionList)
        val distinctList = evolutionList.distinctBy { it.name }

        layoutEvolution.removeAllViews()

        for (i in distinctList.indices) {
            val step = distinctList[i]

            val itemView = layoutInflater.inflate(R.layout.item_evolution_chain, layoutEvolution, false)

            val imgEvo = itemView.findViewById<ImageView>(R.id.imgEvo)
            val tvEvoName = itemView.findViewById<TextView>(R.id.tvEvoName)

            tvEvoName.text = step.name.replaceFirstChar { it.uppercase() }

            val pokemonId = step.url.split("/").dropLast(1).last()
            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

            Glide.with(this).load(imageUrl).into(imgEvo)

            layoutEvolution.addView(itemView)

            if (i < distinctList.size - 1) {
                addArrowView()
            }
        }
    }

    private fun collectEvolutions(chain: Chain, list: MutableList<SpeciesInfo>) {
        list.add(chain.species)
        for (evolution in chain.evolves_to) {
            collectEvolutions(evolution, list)
        }
    }

    private fun addArrowView() {
        val arrow = ImageView(this)
        arrow.setImageResource(android.R.drawable.ic_media_play)
        arrow.alpha = 0.5f
        arrow.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)

        val params = LinearLayout.LayoutParams(40, 40)
        params.gravity = Gravity.CENTER_VERTICAL
        arrow.layoutParams = params

        layoutEvolution.addView(arrow)
    }

    private fun showAlert(title: String, message: String, closeOnDismiss: Boolean = false) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            if (closeOnDismiss) finish()
        }
        builder.setCancelable(false)
        builder.show()
    }
}