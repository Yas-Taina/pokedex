package com.example.pokedex.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.R
import com.example.pokedex.api.RetrofitClient
import com.example.pokedex.models.HomeStats
import com.example.pokedex.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

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
        val userLogin = sessionManager.getUserLogin() ?: return

        RetrofitClient.apiService.getHomeStats(userLogin).enqueue(object : Callback<HomeStats> {
            override fun onResponse(call: Call<HomeStats>, response: Response<HomeStats>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null) {
                        tvTotalPokemons.text = "Total de Pokémons: ${stats.total}"

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_register_pokemon -> {
                startActivity(Intent(this, PokemonListActivity::class.java))
                true
            }
            R.id.menu_list_my_pokemons -> {
                startActivity(Intent(this, MyPokemonsActivity::class.java))
                true
            }
            R.id.menu_search_by_type -> {
                startActivity(Intent(this, SearchByTypeActivity::class.java))
                true
            }
            R.id.menu_search_by_ability -> {
                startActivity(Intent(this, SearchByAbilityActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}