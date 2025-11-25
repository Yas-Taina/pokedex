package com.example.pokedex.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.PokemonDetail
import com.example.pokedex.models.RegisteredPokemon
import com.example.pokedex.models.UpdatePokemonRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPokemonActivity : BaseActivity() {

    private lateinit var tvPokemonName: TextView
    private lateinit var spinnerAbility: Spinner
    private lateinit var checkBoxContainer: LinearLayout
    private lateinit var btnUpdate: Button

    private var pokemonDbId = 0
    private var pokemonApiId = 0
    private val selectedMoves = mutableListOf<String>()
    private var allMoves = listOf<String>()
    private var currentMoves = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pokemon)

        pokemonDbId = intent.getIntExtra("pokemon_id", 0)

        tvPokemonName = findViewById(R.id.tvPokemonName)
        spinnerAbility = findViewById(R.id.spinnerAbility)
        checkBoxContainer = findViewById(R.id.checkBoxContainer)
        btnUpdate = findViewById(R.id.btnUpdate)

        btnUpdate.setOnClickListener {
            updatePokemon()
        }

        loadPokemonData()
    }

    private fun loadPokemonData() {
        RetrofitClient.apiService.getPokemonDetails(pokemonDbId)
            .enqueue(object : Callback<RegisteredPokemon> {
                override fun onResponse(
                    call: Call<RegisteredPokemon>,
                    response: Response<RegisteredPokemon>
                ) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            pokemonApiId = pokemon.pokemon_id
                            tvPokemonName.text = "Pokémon: ${pokemon.pokemon_name.capitalize()}"
                            currentMoves = pokemon.moves.split(",")
                            selectedMoves.addAll(currentMoves)

                            loadPokemonDetailsFromApi(pokemon.ability)
                        }
                    }
                }

                override fun onFailure(call: Call<RegisteredPokemon>, t: Throwable) {
                    Toast.makeText(
                        this@EditPokemonActivity,
                        "Erro ao carregar dados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadPokemonDetailsFromApi(currentAbility: String) {
        RetrofitClient.pokeApiService.getPokemonDetail(pokemonApiId)
            .enqueue(object : Callback<PokemonDetail> {
                override fun onResponse(
                    call: Call<PokemonDetail>,
                    response: Response<PokemonDetail>
                ) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            setupAbilities(pokemon, currentAbility)
                            setupMoves(pokemon)
                        }
                    }
                }

                override fun onFailure(call: Call<PokemonDetail>, t: Throwable) {
                    Toast.makeText(
                        this@EditPokemonActivity,
                        "Erro ao carregar detalhes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupAbilities(pokemon: PokemonDetail, currentAbility: String) {
        val abilities = pokemon.abilities.map { it.ability.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, abilities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAbility.adapter = adapter

        val currentIndex = abilities.indexOf(currentAbility)
        if (currentIndex >= 0) {
            spinnerAbility.setSelection(currentIndex)
        }
    }

    private fun setupMoves(pokemon: PokemonDetail) {
        allMoves = pokemon.moves.take(20).map { it.move.name }

        allMoves.forEach { move ->
            val checkBox = CheckBox(this)
            checkBox.text = move
            checkBox.isChecked = currentMoves.contains(move)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedMoves.size < 3) {
                        selectedMoves.add(move)
                    } else {
                        checkBox.isChecked = false
                        Toast.makeText(
                            this,
                            "Máximo de 3 ataques",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    selectedMoves.remove(move)
                }
            }
            checkBoxContainer.addView(checkBox)
        }
    }

    private fun updatePokemon() {
        val ability = spinnerAbility.selectedItem?.toString()

        if (ability == null) {
            Toast.makeText(this, "Selecione uma habilidade", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMoves.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos 1 ataque", Toast.LENGTH_SHORT).show()
            return
        }

        val movesString = selectedMoves.joinToString(",")
        val request = UpdatePokemonRequest(ability, movesString)

        RetrofitClient.apiService.updatePokemon(pokemonDbId, request)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@EditPokemonActivity,
                            "Pokémon atualizado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditPokemonActivity,
                            "Erro ao atualizar pokémon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        this@EditPokemonActivity,
                        "Erro de conexão",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}