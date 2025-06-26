package com.example.tictactoe

class GameModel {
    private var gameData = GameData()

    fun getCurrentGameData(): GameData = gameData

    fun makeMove(row: Int, col: Int): Boolean {
        if (!gameData.isGameActive || gameData.boardState[row][col].isNotEmpty()) {
            return false
        }

        // Make the move
        val newBoardState = gameData.boardState.map { it.clone() }.toTypedArray()
        newBoardState[row][col] = gameData.currentPlayer.name

        // Check for winner
        val winner = checkWinner(newBoardState, gameData.currentPlayer)
        val isDraw = winner == null && isBoardFull(newBoardState)
        val isGameActive = winner == null && !isDraw
        val nextPlayer = if (isGameActive) gameData.currentPlayer.getOpponent() else gameData.currentPlayer

        gameData = gameData.copy(
            boardState = newBoardState,
            currentPlayer = nextPlayer,
            isGameActive = isGameActive,
            winner = winner,
            isDraw = isDraw,
            moveCount = gameData.moveCount + 1
        )

        return true
    }

    fun resetGame() {
        gameData = GameData()
    }

    fun getWinningPositions(): List<Pair<Int, Int>>? {
        val board = gameData.boardState
        val winner = gameData.winner ?: return null

        // Check rows
        for (row in 0..2) {
            if (board[row].all { it == winner.name }) {
                return (0..2).map { col -> Pair(row, col) }
            }
        }

        // Check columns
        for (col in 0..2) {
            if ((0..2).all { row -> board[row][col] == winner.name }) {
                return (0..2).map { row -> Pair(row, col) }
            }
        }

        // Check diagonals
        if ((0..2).all { i -> board[i][i] == winner.name }) {
            return (0..2).map { i -> Pair(i, i) }
        }

        if ((0..2).all { i -> board[i][2-i] == winner.name }) {
            return (0..2).map { i -> Pair(i, 2-i) }
        }

        return null
    }

    private fun checkWinner(board: Array<Array<String>>, player: Player): Player? {
        val playerName = player.name

        // Check rows
        for (row in 0..2) {
            if (board[row].all { it == playerName }) {
                return player
            }
        }

        // Check columns
        for (col in 0..2) {
            if ((0..2).all { row -> board[row][col] == playerName }) {
                return player
            }
        }

        // Check diagonals
        if ((0..2).all { i -> board[i][i] == playerName } ||
            (0..2).all { i -> board[i][2-i] == playerName }) {
            return player
        }

        return null
    }

    private fun isBoardFull(board: Array<Array<String>>): Boolean {
        return board.all { row -> row.all { cell -> cell.isNotEmpty() } }
    }
}