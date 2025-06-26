package com.example.tictactoe

data class GameData(
    val boardState: Array<Array<String>> = Array(3) { Array(3) { "" } },
    val currentPlayer: Player = Player.ICE,
    val isGameActive: Boolean = true,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val moveCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameData

        if (!boardState.contentDeepEquals(other.boardState)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (isGameActive != other.isGameActive) return false
        if (winner != other.winner) return false
        if (isDraw != other.isDraw) return false
        if (moveCount != other.moveCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boardState.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + isGameActive.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        result = 31 * result + isDraw.hashCode()
        result = 31 * result + moveCount
        return result
    }
}