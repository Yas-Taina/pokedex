package com.example.pokedex.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.PokemonDetail
import com.example.pokedex.models.RegisterPokemonRequest
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectAbilityMoveActivity : BaseActivity() {

    private lateinit var tvPokemonName: TextView
    private lateinit var spinnerAbility: Spinner
    private lateinit var checkBoxContainer: LinearLayout
    private lateinit var btnConfirm: Button
    private lateinit var sessionManager: SessionManager

    private var pokemonId = 0
    private var pokemonName = ""
    private var pokemonTypes = ""
    private val selectedMoves = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ability_move)

        sessionManager = SessionManager(this)

        pokemonId = intent.getIntExtra("pokemon_id", 0)
        pokemonName = intent.getStringExtra("pokemon_name") ?: ""

        tvPokemonName = findViewById(R.id.tvPokemonName)
        spinnerAbility = findViewById(R.id.spinnerAbility)
        checkBoxContainer = findViewById(R.id.checkBoxContainer)
        btnConfirm = findViewById(R.id.btnConfirm)
        tvPokemonName.text = "Pokémon: ${pokemonName.replaceFirstChar { it.uppercase() }}"

        loadPokemonDetails()

        btnConfirm.setOnClickListener {
            registerPokemon()
        }
    }

    private fun loadPokemonDetails() {
        RetrofitClient.pokeApiService.getPokemonDetail(pokemonId)
            .enqueue(object : Callback<PokemonDetail> {
                override fun onResponse(
                    call: Call<PokemonDetail>,
                    response: Response<PokemonDetail>
                ) {
                    if (response.isSuccessful) {
                        val pokemon = response.body()
                        if (pokemon != null) {
                            setupAbilities(pokemon)
                            setupMoves(pokemon)
                            pokemonTypes = pokemon.types.joinToString(",") { it.type.name }
                        }
                    }
                }

                override fun onFailure(call: Call<PokemonDetail>, t: Throwable) {
                    Toast.makeText(this@SelectAbilityMoveActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupAbilities(pokemon: PokemonDetail) {
        val abilities = pokemon.abilities.map { it.ability.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, abilities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAbility.adapter = adapter
    }

    private fun setupMoves(pokemon: PokemonDetail) {
        val moves = pokemon.moves.take(20).map { it.move.name }

        moves.forEach { move ->
            val checkBox = CheckBox(this)
            checkBox.text = move

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

    private fun registerPokemon() {
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

        val userLogin = sessionManager.getUserLogin() ?: return
        val movesString = selectedMoves.joinToString(",")

        val request = RegisterPokemonRequest(
            pokemon_id = pokemonId,
            pokemon_name = pokemonName,
            types = pokemonTypes,
            ability = ability,
            moves = movesString,
            user_login = userLogin
        )

        RetrofitClient.apiService.registerPokemon(request)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        val msg = response.body()?.get("message") as? String ?: "Sucesso!"
                        showAlert("Registro Concluído", msg, closeOnDismiss = true)
                    } else {
                        val errorMsg = when (response.code()) {
                            409 -> "Você já possui um ${pokemonName.uppercase()} registrado."
                            else -> "Falha ao registrar (Erro ${response.code()})"
                        }
                        showAlert("Erro no Registro", errorMsg)
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    showAlert("Erro de Conexão", "Falha ao conectar com o servidor.")
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