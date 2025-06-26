package com.example.tictactoe

enum class Player(val displayName: String, val logoRes: Int) {
    ICE("Player 1", R.drawable.ice),
    FIRE("Player 2", R.drawable.fire);

    fun getOpponent(): Player {
        return when (this) {
            ICE -> FIRE
            FIRE -> ICE
        }
    }
}