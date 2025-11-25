package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout // Importe isso
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.models.PokemonBasic
import com.example.pokedex.models.RegisteredPokemon

class SearchResultAdapter(
    private var items: List<Any>,
    private val isAbilitySearch: Boolean = false
) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPokemon: ImageView = view.findViewById(R.id.imgPokemon)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvDataLabel: TextView = view.findViewById(R.id.tvDataLabel)
        val tvTypes: TextView = view.findViewById(R.id.tvTypes)
        val chipTypeContainer: LinearLayout = view.findViewById(R.id.chipTypeContainer)
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
                val pokemonId = item.url.split("/").dropLast(1).last()

                holder.tvName.text = item.name.replaceFirstChar { it.uppercase() }
                holder.tvId.text = "#$pokemonId"
                holder.chipTypeContainer.visibility = View.GONE

                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
                Glide.with(holder.itemView.context).load(imageUrl).into(holder.imgPokemon)
            }

            is RegisteredPokemon -> {
                holder.tvName.text = item.pokemon_name.replaceFirstChar { it.uppercase() }
                holder.tvId.text = "#${item.pokemon_id}"

                holder.chipTypeContainer.visibility = View.VISIBLE

                if (isAbilitySearch) {
                    holder.tvDataLabel.text = "ABILITY:"
                    holder.tvTypes.text = item.ability
                } else {
                    holder.tvDataLabel.text = "TYPE:"
                    holder.tvTypes.text = item.types.split(",")
                        .joinToString(", ") { it.trim().uppercase() }
                }

                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${item.pokemon_id}.png"
                Glide.with(holder.itemView.context).load(imageUrl).into(holder.imgPokemon)
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