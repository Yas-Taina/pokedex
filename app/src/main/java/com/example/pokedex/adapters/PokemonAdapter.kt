package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.models.PokemonBasic

class PokemonAdapter(
    private var pokemons: List<PokemonBasic>,
    private var registeredIds: List<Int>,
    private val onRegisterClick: (PokemonBasic) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokemon)
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val btnRegister: Button = view.findViewById(R.id.btnRegister)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemons[position]
        val pokemonId = pokemon.url.split("/").dropLast(1).last().toInt()
        val isRegistered = registeredIds.contains(pokemonId)

        holder.tvId.text = "#$pokemonId"
        holder.tvName.text = pokemon.name.capitalize()

        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imgPokemon)

        if (isRegistered) {
            holder.imgPokemon.alpha = 1.0f
            holder.btnRegister.isEnabled = false
            holder.btnRegister.text = "Registrado"
        } else {
            holder.imgPokemon.alpha = 0.4f
            holder.btnRegister.isEnabled = true
            holder.btnRegister.text = "Registrar"
            holder.btnRegister.setOnClickListener {
                onRegisterClick(pokemon)
            }
        }
    }

    override fun getItemCount() = pokemons.size

    fun updatePokemons(newPokemons: List<PokemonBasic>) {
        pokemons = newPokemons
        notifyDataSetChanged()
    }

    fun updateRegisteredIds(newIds: List<Int>) {
        registeredIds = newIds
        notifyDataSetChanged()
    }
}