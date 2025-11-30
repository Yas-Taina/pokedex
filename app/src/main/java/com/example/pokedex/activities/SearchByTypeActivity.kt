package com.example.pokedex.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.adapters.SearchResultAdapter
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.api.TypePokemonResponse
import com.example.pokedex.models.RegisteredPokemon
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByTypeActivity : BaseActivity() {

    private lateinit var editType: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSearch: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_type)

        sessionManager = SessionManager(this)

        editType = findViewById(R.id.editType)
        radioGroup = findViewById(R.id.radioGroup)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SearchResultAdapter(emptyList(), isAbilitySearch = false)
        recyclerView.adapter = adapter

        btnSearch.setOnClickListener {
            searchByType()
        }
    }

    private fun searchByType() {
        val type = editType.text.toString().trim().toLowerCase()

        if (type.isEmpty()) {
            Toast.makeText(this, "Digite um tipo para pesquisar", Toast.LENGTH_SHORT).show()
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
            searchAllPokemons(type)
        } else {
            searchRegisteredPokemons(type)
        }
    }

    private fun searchAllPokemons(type: String) {
        RetrofitClient.pokeApiService.getPokemonsByType(type)
            .enqueue(object : Callback<TypePokemonResponse> {
                override fun onResponse(
                    call: Call<TypePokemonResponse>,
                    response: Response<TypePokemonResponse>
                ) {
                    if (response.isSuccessful) {
                        val pokemons = response.body()?.pokemon?.map { it.pokemon } ?: emptyList()

                        if (pokemons.isEmpty()) {
                            Toast.makeText(
                                this@SearchByTypeActivity,
                                "Nenhum pokémon encontrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        adapter.updateFromPokeApi(pokemons)
                    } else {
                        Toast.makeText(
                            this@SearchByTypeActivity,
                            "Tipo não encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TypePokemonResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SearchByTypeActivity,
                        "Erro ao pesquisar: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun searchRegisteredPokemons(type: String) {
        RetrofitClient.apiService.searchByType(type)
            .enqueue(object : Callback<List<RegisteredPokemon>> {
                override fun onResponse(
                    call: Call<List<RegisteredPokemon>>,
                    response: Response<List<RegisteredPokemon>>
                ) {
                    if (response.isSuccessful) {
                        val pokemons = response.body() ?: emptyList()

                        if (pokemons.isEmpty()) {
                            Toast.makeText(
                                this@SearchByTypeActivity,
                                "Nenhum pokémon registrado com esse tipo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        adapter.updateFromDatabase(pokemons)
                    }
                }

                override fun onFailure(call: Call<List<RegisteredPokemon>>, t: Throwable) {
                    Toast.makeText(
                        this@SearchByTypeActivity,
                        "Erro ao pesquisar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}