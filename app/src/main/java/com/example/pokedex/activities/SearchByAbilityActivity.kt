package com.example.pokedex.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.adapters.SearchResultAdapter
import com.example.pokedex.api.AbilityPokemonResponse
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.RegisteredPokemon
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByAbilityActivity : BaseActivity() {

    private lateinit var editAbility: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSearch: Button
    private lateinit var btnVoltar: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterSection: LinearLayout
    private lateinit var adapter: SearchResultAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_ability)

        sessionManager = SessionManager(this)

        editAbility = findViewById(R.id.editAbility)
        radioGroup = findViewById(R.id.radioGroup)
        btnSearch = findViewById(R.id.btnSearch)
        btnVoltar = findViewById(R.id.btnVoltar)
        recyclerView = findViewById(R.id.recyclerView)
        filterSection = findViewById(R.id.filterSection)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SearchResultAdapter(emptyList(), isAbilitySearch = true)
        recyclerView.adapter = adapter

        btnSearch.setOnClickListener {
            searchByAbility()
        }

        btnVoltar.setOnClickListener {
            btnVoltar.visibility = View.GONE
            filterSection.visibility = View.VISIBLE
            adapter.updateFromDatabase(emptyList())
        }
    }

    private fun searchByAbility() {
        val ability = editAbility.text.toString().trim().toLowerCase()

        if (ability.isEmpty()) {
            Toast.makeText(this, "Digite uma habilidade para pesquisar", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioId = radioGroup.checkedRadioButtonId

        if (selectedRadioId == -1) {
            Toast.makeText(this, "Selecione uma opção de pesquisa", Toast.LENGTH_SHORT).show()
            return
        }

        val radioButton = findViewById<RadioButton>(selectedRadioId)
        val searchInAll = radioButton.text.toString().contains("Todos")

        if (searchInAll) {
            searchAllPokemons(ability)
        } else {
            searchRegisteredPokemons(ability)
        }
    }

    private fun searchAllPokemons(ability: String) {
        RetrofitClient.pokeApiService.getPokemonsByAbility(ability)
            .enqueue(object : Callback<AbilityPokemonResponse> {
                override fun onResponse(
                    call: Call<AbilityPokemonResponse>,
                    response: Response<AbilityPokemonResponse>
                ) {
                    if (response.isSuccessful) {
                        val pokemons = response.body()?.pokemon?.map { it.pokemon } ?: emptyList()

                        if (pokemons.isEmpty()) {
                            Toast.makeText(
                                this@SearchByAbilityActivity,
                                "Nenhum pokémon encontrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        if (!pokemons.isEmpty()) {
                            filterSection.visibility = View.GONE
                            btnVoltar.visibility = View.VISIBLE
                        }

                        adapter.updateFromPokeApi(pokemons)
                    } else {
                        Toast.makeText(
                            this@SearchByAbilityActivity,
                            "Habilidade não encontrada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AbilityPokemonResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SearchByAbilityActivity,
                        "Erro ao pesquisar: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun searchRegisteredPokemons(ability: String) {
        RetrofitClient.apiService.searchByAbility(ability)
            .enqueue(object : Callback<List<RegisteredPokemon>> {
                override fun onResponse(
                    call: Call<List<RegisteredPokemon>>,
                    response: Response<List<RegisteredPokemon>>
                ) {
                    if (response.isSuccessful) {
                        val pokemons = response.body() ?: emptyList()

                        if (pokemons.isEmpty()) {
                            Toast.makeText(
                                this@SearchByAbilityActivity,
                                "Nenhum pokémon registrado com essa habilidade",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        if (!pokemons.isEmpty()) {
                            filterSection.visibility = View.GONE
                            btnVoltar.visibility = View.VISIBLE
                        }

                        adapter.updateFromDatabase(pokemons)
                    }
                }

                override fun onFailure(call: Call<List<RegisteredPokemon>>, t: Throwable) {
                    Toast.makeText(
                        this@SearchByAbilityActivity,
                        "Erro ao pesquisar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}