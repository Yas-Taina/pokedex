package com.example.pokedex.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.pokedex.R
import com.example.pokedex.api.AbilityListResponse
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.PokemonDetail
import com.example.pokedex.models.RegisterPokemonRequest
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectAbilityMoveActivity : BaseActivity() {

    private lateinit var tvPokemonName: TextView
    private lateinit var checkBoxAbilityContainer: LinearLayout
    private lateinit var checkBoxMoveContainer: LinearLayout
    private lateinit var btnConfirm: Button
    private lateinit var sessionManager: SessionManager

    private var pokemonId = 0
    private var pokemonName = ""
    private var pokemonTypes = ""
    private val selectedAbilities = mutableListOf<String>()
    private val selectedMoves = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ability_move)

        sessionManager = SessionManager(this)

        pokemonId = intent.getIntExtra("pokemon_id", 0)
        pokemonName = intent.getStringExtra("pokemon_name") ?: ""

        try {
            tvPokemonName = findViewById(R.id.tvPokemonName)
            checkBoxAbilityContainer = findViewById(R.id.checkBoxAbilityContainer)
            checkBoxMoveContainer = findViewById(R.id.checkBoxMoveContainer)
            btnConfirm = findViewById(R.id.btnConfirm)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao inicializar views: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        tvPokemonName.text = "Pokémon: ${pokemonName.replaceFirstChar { it.uppercase() }}"

        loadPokemonDetails()
        loadAllAbilities()

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

    private fun loadAllAbilities() {
        RetrofitClient.pokeApiService.getAllAbilities()
            .enqueue(object : Callback<AbilityListResponse> {
                override fun onResponse(
                    call: Call<AbilityListResponse>,
                    response: Response<AbilityListResponse>
                ) {
                    if (response.isSuccessful) {
                        val abilities = response.body()?.results?.map { it.name } ?: emptyList()
                        setupAbilities(abilities)
                    }
                }

                override fun onFailure(call: Call<AbilityListResponse>, t: Throwable) {
                    Toast.makeText(this@SelectAbilityMoveActivity, "Erro ao carregar habilidades", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupAbilities(abilities: List<String>) {
        checkBoxAbilityContainer.removeAllViews()

        abilities.forEach { ability ->
            val checkBox = CheckBox(this)
            checkBox.text = ability

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedAbilities.size < 3) {
                        selectedAbilities.add(ability)
                    } else {
                        checkBox.isChecked = false
                        Toast.makeText(this, "Máximo de 3 habilidades permitidas.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedAbilities.remove(ability)
                }
            }
            checkBoxAbilityContainer.addView(checkBox)
        }
    }

    private fun setupMoves(pokemon: PokemonDetail) {
        val moves = pokemon.moves.map { it.move.name }

        moves.forEach { move ->
            val checkBox = CheckBox(this)
            checkBox.text = move

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMoves.add(move)
                } else {
                    selectedMoves.remove(move)
                }
            }
            checkBoxMoveContainer.addView(checkBox)
        }
    }

    private fun registerPokemon() {
        if (selectedAbilities.isEmpty()) {
            showAlert("Atenção", "Selecione pelo menos 1 habilidade.")
            return
        }
        if (selectedAbilities.size > 3) {
            showAlert("Atenção", "Selecione no máximo 3 habilidades.")
            return
        }
        if (selectedMoves.isEmpty()) {
            showAlert("Atenção", "Selecione pelo menos 1 ataque.")
            return
        }

        val userLogin = sessionManager.getUserLogin() ?: return
        val abilitiesString = selectedAbilities.joinToString(",")
        val movesString = selectedMoves.joinToString(",")

        val request = RegisterPokemonRequest(
            pokemon_id = pokemonId,
            pokemon_name = pokemonName,
            types = pokemonTypes,
            ability = abilitiesString,
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