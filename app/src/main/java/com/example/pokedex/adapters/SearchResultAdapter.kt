package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.models.PokemonBasic
import com.example.pokedex.models.RegisteredPokemon

class SearchResultAdapter(
    private var items: List<Any>
) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokemon)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTypes: TextView = view.findViewById(R.id.tvTypes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = items[position]

        when (item) {
            is PokemonBasic -> {
                val pokemonId = item.url.split("/").dropLast(1).last().toInt()
                holder.tvName.text = item.name.capitalize()
                holder.tvTypes.text = "ID: #$pokemonId"

                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .into(holder.imgPokemon)
            }
            is RegisteredPokemon -> {
                holder.tvName.text = item.pokemon_name.capitalize()
                holder.tvTypes.text = "Tipos: ${item.types.split(",").joinToString(", ")}"

                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${item.pokemon_id}.png"
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .into(holder.imgPokemon)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateFromPokeApi(pokemons: List<PokemonBasic>) {
        items = pokemons
        notifyDataSetChanged()
    }

    fun updateFromDatabase(pokemons: List<RegisteredPokemon>) {
        items = pokemons
        notifyDataSetChanged()
    }
}