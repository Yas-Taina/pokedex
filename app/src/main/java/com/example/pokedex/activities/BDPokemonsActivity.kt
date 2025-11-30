package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.adapters.BdPokemonAdapter
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.RegisteredPokemon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BDPokemonsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BdPokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bd_pokemons)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BdPokemonAdapter(
            emptyList(),
            onDetailsClick = { pokemon ->
                openDetails(pokemon.id)
            }
        )
        recyclerView.adapter = adapter

        loadAllPokemons()
    }

    override fun onResume() {
        super.onResume()
        loadAllPokemons()
    }

    private fun openDetails(pokemonId: Int) {
        val intent = Intent(this, PokemonDetailsActivity::class.java)
        intent.putExtra("pokemon_id", pokemonId)
        startActivity(intent)
    }

    private fun loadAllPokemons() {

        RetrofitClient.apiService.getAllPokemons()
            .enqueue(object : Callback<List<RegisteredPokemon>> {
                override fun onResponse(
                    call: Call<List<RegisteredPokemon>>,
                    response: Response<List<RegisteredPokemon>>
                ) {
                    if (response.isSuccessful) {
                        val pokemons = response.body() ?: emptyList()
                        adapter.updatePokemons(pokemons)

                        if (pokemons.isEmpty()) {
                            Toast.makeText(
                                this@BDPokemonsActivity,
                                "Nenhum pokémon encontrado no servidor",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@BDPokemonsActivity,
                            "Erro na resposta do servidor: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<RegisteredPokemon>>, t: Throwable) {
                    Toast.makeText(
                        this@BDPokemonsActivity,
                        "Erro de conexão: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}