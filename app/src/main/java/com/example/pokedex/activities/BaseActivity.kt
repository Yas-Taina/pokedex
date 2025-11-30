package com.example.pokedex.activities
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.R
import com.example.pokedex.utils.SessionManager

open class BaseActivity : AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupToolbar()
    }
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.pokedex_toolbar)

        if (toolbar != null) {
            setSupportActionBar(toolbar)
            if (this !is MainActivity) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        if (menu != null) {
            try {
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        val sessionManager = SessionManager(this)

        return when (item.itemId) {
            R.id.menu_register_pokemon -> {
                if (this !is PokemonListActivity) {
                    startActivity(Intent(this, PokemonListActivity::class.java))
                }
                true
            }
            R.id.menu_list_my_pokemons -> {
                startActivity(Intent(this, BDPokemonsActivity::class.java))
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
            R.id.menu_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                sessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }

            R.id.menu_close_app -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}