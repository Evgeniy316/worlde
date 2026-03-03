package com.example.wordle.data

enum class TileColor { GRAY, YELLOW, GREEN }

data class Tile(val letter: Char, val color: TileColor)