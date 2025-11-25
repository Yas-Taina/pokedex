package com.example.pokedex.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        tvPokemonName.text = "Pokémon: ${pokemonName.capitalize()}"

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
                    Toast.makeText(
                        this@SelectAbilityMoveActivity,
                        "Erro ao carregar detalhes",
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun registerPokemon() {
        val ability = spinnerAbility.selectedItem?.toString()

        if (ability == null) {
            Toast.makeText(this, "Selecione uma habilidade", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMoves.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos 1 ataque", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(
                            this@SelectAbilityMoveActivity,
                            "Pokémon registrado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@SelectAbilityMoveActivity,
                            "Erro ao registrar pokémon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        this@SelectAbilityMoveActivity,
                        "Erro de conexão",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}