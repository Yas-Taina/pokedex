package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.HomeStats
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class  MainActivity : BaseActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvTotalPokemons: TextView
    private lateinit var tvTopTypes: TextView
    private lateinit var tvTopAbilities: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvTotalPokemons = findViewById(R.id.tvTotalPokemons)
        tvTopTypes = findViewById(R.id.tvTopTypes)
        tvTopAbilities = findViewById(R.id.tvTopAbilities)

        val userName = sessionManager.getUserName()
        tvWelcome.text = "Bem-vindo, $userName!"

        loadHomeStats()
    }

    override fun onResume() {
        super.onResume()
        loadHomeStats()
    }

    private fun loadHomeStats() {
        RetrofitClient.apiService.getHomeStats().enqueue(object : Callback<HomeStats> {
            override fun onResponse(call: Call<HomeStats>, response: Response<HomeStats>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null) {
                        tvTotalPokemons.text = "Pokémon Registrados: ${stats.total}"

                        val topTypesText = if (stats.topTypes.isNotEmpty()) {
                            "Top 3 Tipos:\n" + stats.topTypes.joinToString("\n") {
                                "- ${it.type}: ${it.count}"
                            }
                        } else {
                            "Nenhum tipo registrado"
                        }
                        tvTopTypes.text = topTypesText

                        val topAbilitiesText = if (stats.topAbilities.isNotEmpty()) {
                            "Top 3 Habilidades:\n" + stats.topAbilities.joinToString("\n") {
                                "- ${it.ability}: ${it.count}"
                            }
                        } else {
                            "Nenhuma habilidade registrada"
                        }
                        tvTopAbilities.text = topAbilitiesText
                    }
                }
            }

            override fun onFailure(call: Call<HomeStats>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao carregar estatísticas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}