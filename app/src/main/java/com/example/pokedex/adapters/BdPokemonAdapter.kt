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
import com.example.pokedex.models.RegisteredPokemon

class BdPokemonAdapter(
    private var pokemons: List<RegisteredPokemon>,
    private val onDetailsClick: (RegisteredPokemon) -> Unit
) : RecyclerView.Adapter<BdPokemonAdapter.MyPokemonViewHolder>() {

    class MyPokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokemon)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvUser: TextView = view.findViewById(R.id.tvUser)
        val btnDetails: Button = view.findViewById(R.id.btnDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bd_pokemon, parent, false)
        return MyPokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyPokemonViewHolder, position: Int) {
        val pokemon = pokemons[position]

        holder.tvName.text = pokemon.pokemon_name.replaceFirstChar { it.uppercase() }
        holder.tvUser.text = pokemon.user_login

        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.pokemon_id}.png"
        Glide.with(holder.itemView.context).load(imageUrl).into(holder.imgPokemon)

        holder.btnDetails.setOnClickListener { onDetailsClick(pokemon) }
        holder.imgPokemon.setOnClickListener { onDetailsClick(pokemon) }
    }

    override fun getItemCount() = pokemons.size

    fun updatePokemons(newPokemons: List<RegisteredPokemon>) {
        pokemons = newPokemons
        notifyDataSetChanged()
    }
}