package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.adapters.MyPokemonAdapter
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.RegisteredPokemon
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPokemonsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPokemonAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pokemons)

        sessionManager = SessionManager(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MyPokemonAdapter(
            emptyList(),
            onImageClick = { pokemon ->
                val intent = Intent(this, PokemonDetailsActivity::class.java)
                intent.putExtra("pokemon_id", pokemon.id)
                startActivity(intent)
            },
            onEditClick = { pokemon ->
                val intent = Intent(this, EditPokemonActivity::class.java)
                intent.putExtra("pokemon_id", pokemon.id)
                startActivity(intent)
            },
            onDeleteClick = { pokemon ->
                confirmDelete(pokemon)
            }
        )
        recyclerView.adapter = adapter

        loadMyPokemons()
    }

    override fun onResume() {
        super.onResume()
        loadMyPokemons()
    }

    private fun loadMyPokemons() {
        val userLogin = sessionManager.getUserLogin() ?: return

        RetrofitClient.apiService.getUserPokemons(userLogin)
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
                                this@MyPokemonsActivity,
                                "Nenhum pokémon registrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<RegisteredPokemon>>, t: Throwable) {
                    Toast.makeText(
                        this@MyPokemonsActivity,
                        "Erro ao carregar pokémons",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun confirmDelete(pokemon: RegisteredPokemon) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente excluir ${pokemon.pokemon_name}?")
            .setPositiveButton("Sim") { _, _ ->
                deletePokemon(pokemon.id)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deletePokemon(id: Int) {
        RetrofitClient.apiService.deletePokemon(id)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@MyPokemonsActivity,
                            "Pokémon excluído com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadMyPokemons()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(
                        this@MyPokemonsActivity,
                        "Erro ao excluir pokémon",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}