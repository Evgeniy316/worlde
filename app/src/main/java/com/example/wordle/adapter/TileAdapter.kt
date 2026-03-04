package com.example.wordle.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wordle.R
import com.example.wordle.data.Tile
import com.example.wordle.data.TileColor
import com.google.android.material.card.MaterialCardView

class TileAdapter : RecyclerView.Adapter<TileAdapter.TileViewHolder>() {

    private var tiles: List<Tile> = emptyList()

    fun update(newTiles: List<Tile>) {
        tiles = newTiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tile, parent, false)
        return TileViewHolder(view)
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        val tile = tiles[position]
        val isEmpty = tile.letter == ' '

        // Если ячейка пустая — показываем подчёркивание внизу
        holder.tvLetter.text = if (isEmpty) "_" else tile.letter.toString()
        holder.tvLetter.gravity = if (isEmpty) {
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        } else {
            Gravity.CENTER
        }

        // Цвет фона: тёмный для пустых слотов, а после проверки — по цвету результата
        val backgroundColorRes = when {
            isEmpty -> R.color.dark
            tile.color == TileColor.GREEN -> R.color.correct_green
            tile.color == TileColor.YELLOW -> R.color.present_yellow
            else -> R.color.absent_gray
        }

        (holder.itemView as MaterialCardView).setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, backgroundColorRes)
        )
    }

    override fun getItemCount() = tiles.size

    class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLetter: TextView = itemView.findViewById(R.id.tv_letter)
    }
}