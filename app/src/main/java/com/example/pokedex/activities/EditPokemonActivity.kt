package com.example.pokedex.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
    private var currentMoves = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pokemon)
        pokemonDbId = intent.getIntExtra("pokemon_id", 0)
        tvPokemonName = findViewById(R.id.tvPokemonName)
        spinnerAbility = findViewById(R.id.spinnerAbility)
        checkBoxContainer = findViewById(R.id.checkBoxContainer)
        btnUpdate = findViewById(R.id.btnUpdate)

        if (pokemonDbId != 0) {
            loadPokemonData()
        } else {
            showAlert("Erro", "ID inválido para edição.", true)
        }

        btnUpdate.setOnClickListener {
            updatePokemon()
        }
    }

    private fun loadPokemonData() {
        RetrofitClient.apiService.getPokemonDetails(pokemonDbId)
            .enqueue(object : Callback<RegisteredPokemon> {
                override fun onResponse(call: Call<RegisteredPokemon>, response: Response<RegisteredPokemon>) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            pokemonApiId = pokemon.pokemon_id
                            tvPokemonName.text = "Editando: ${pokemon.pokemon_name.replaceFirstChar { it.uppercase() }}"
                            currentMoves = pokemon.moves.split(",").map { it.trim() }
                            selectedMoves.clear()
                            selectedMoves.addAll(currentMoves)
                            loadPokemonDetailsFromApi(pokemon.ability)
                        }
                    } else {
                        showAlert("Erro", "Não foi possível carregar os dados atuais.")
                    }
                }

                override fun onFailure(call: Call<RegisteredPokemon>, t: Throwable) {
                    showAlert("Erro", "Falha de conexão com o servidor.")
                }
            })
    }

    private fun loadPokemonDetailsFromApi(currentAbility: String) {
        RetrofitClient.pokeApiService.getPokemonDetail(pokemonApiId)
            .enqueue(object : Callback<PokemonDetail> {
                override fun onResponse(call: Call<PokemonDetail>, response: Response<PokemonDetail>) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            setupAbilities(pokemon, currentAbility)
                            setupMoves(pokemon)
                        }
                    }
                }
                override fun onFailure(call: Call<PokemonDetail>, t: Throwable) {
                    showAlert("Erro", "Falha ao carregar lista de ataques da API.")
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
        val allMoves = pokemon.moves.take(20).map { it.move.name }

        checkBoxContainer.removeAllViews()

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
                        Toast.makeText(this, "Máximo de 3 ataques permitidos.", Toast.LENGTH_SHORT).show()
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
            showAlert("Atenção", "Selecione uma habilidade.")
            return
        }

        if (selectedMoves.isEmpty()) {
            showAlert("Atenção", "Selecione pelo menos 1 ataque.")
            return
        }

        if (selectedMoves.size > 3) {
            showAlert("Atenção", "Selecione no máximo 3 ataques.")
            return
        }

        val movesString = selectedMoves.joinToString(",")
        val request = UpdatePokemonRequest(ability, movesString)

        RetrofitClient.apiService.updatePokemon(pokemonDbId, request)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val msg = response.body()?.get("message") as? String ?: "Dados atualizados!"
                        showAlert("Sucesso", msg, closeOnDismiss = true)
                    } else {
                        showAlert("Erro", "Falha ao atualizar (Código ${response.code()})")
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    showAlert("Erro", "Sem conexão com o servidor.")
                }
            })
    }

    private fun showAlert(title: String, message: String, closeOnDismiss: Boolean = false) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            if (closeOnDismiss) {
                finish()
            }
        }
        builder.setCancelable(false)
        builder.show()
    }
}