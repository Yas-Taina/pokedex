package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.adapters.PokemonAdapter
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.PokemonBasic
import com.example.pokedex.models.PokemonListResponse
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokemonListActivity : BaseActivity() {

    private lateinit var editSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private lateinit var sessionManager: SessionManager
    private var allPokemons = listOf<PokemonBasic>()
    private var registeredIds = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_list)

        sessionManager = SessionManager(this)

        editSearch = findViewById(R.id.editSearch)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PokemonAdapter(
            emptyList(),
            registeredIds,
            onRegisterClick = { pokemon ->
                onPokemonClick(pokemon)
            }
        )
        recyclerView.adapter = adapter

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPokemons(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadRegisteredPokemons()
    }

    override fun onResume() {
        super.onResume()
        loadRegisteredPokemons()
    }

    private fun loadRegisteredPokemons() {
        RetrofitClient.apiService.getRegisteredPokemonIds()
            .enqueue(object : Callback<List<Int>> {
                override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                    if (response.isSuccessful) {
                        registeredIds = response.body() ?: emptyList()
                        adapter.updateRegisteredIds(registeredIds)

                        if (allPokemons.isEmpty()) {
                            loadAllPokemons()
                        }
                    }
                }

                override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                    Toast.makeText(this@PokemonListActivity, "Erro ao verificar registros", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAllPokemons() {
        RetrofitClient.pokeApiService.getAllPokemons()
            .enqueue(object : Callback<PokemonListResponse> {
                override fun onResponse(
                    call: Call<PokemonListResponse>,
                    response: Response<PokemonListResponse>
                ) {
                    if (response.isSuccessful) {
                        allPokemons = response.body()?.results ?: emptyList()
                        adapter.updatePokemons(allPokemons)
                    }
                }

                override fun onFailure(call: Call<PokemonListResponse>, t: Throwable) {
                    Toast.makeText(this@PokemonListActivity, "Erro ao carregar PokeAPI", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterPokemons(query: String) {
        val filtered = if (query.isEmpty()) {
            allPokemons
        } else {
            allPokemons.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.updatePokemons(filtered)
    }

    private fun onPokemonClick(pokemon: PokemonBasic) {
        val pokemonId = pokemon.url.split("/").dropLast(1).last().toInt()

        if (registeredIds.contains(pokemonId)) {
            Toast.makeText(this, "Você já possui este Pokémon!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, SelectAbilityMoveActivity::class.java)
        intent.putExtra("pokemon_id", pokemonId)
        intent.putExtra("pokemon_name", pokemon.name)
        startActivity(intent)
    }
}