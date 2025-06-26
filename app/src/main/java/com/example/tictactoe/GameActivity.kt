package com.example.tictactoe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var gameBoard: GridLayout
    private lateinit var currentPlayerText: TextView
    private lateinit var resetButton: Button
    private lateinit var player1Name: TextView
    private lateinit var player2Name: TextView
    private lateinit var player1Avatar: ImageView
    private lateinit var player2Avatar: ImageView
    private lateinit var backButton: ImageView

    private val boardSize = 3
    private val gameButtons = Array(boardSize) { Array<ImageView?>(boardSize) { null } }
    private val gameModel = GameModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initializeViews()
        setupGameBoard()
        setupPlayers()
        setupClickListeners()
        updateUI()
    }

    private fun initializeViews() {
        gameBoard = findViewById(R.id.gameBoard)
        currentPlayerText = findViewById(R.id.currentPlayerText)
        resetButton = findViewById(R.id.resetButton)
        player1Name = findViewById(R.id.player1Name)
        player2Name = findViewById(R.id.player2Name)
        player1Avatar = findViewById(R.id.player1Avatar)
        player2Avatar = findViewById(R.id.player2Avatar)
        backButton       = findViewById(R.id.back_button)
    }

    private fun setupPlayers() {
        player1Name.text = "Player 1"
        player2Name.text = "Player 2"
        player1Avatar.setImageResource(R.drawable.avatar1)
        player2Avatar.setImageResource(R.drawable.avatar1)
    }

    private fun setupGameBoard() {
        gameBoard.removeAllViews()

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val button = ImageView(this)
                button.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(col, 1f)
                    rowSpec = GridLayout.spec(row, 1f)
                    setMargins(4, 4, 4, 4)
                }

                button.setBackgroundResource(R.drawable.game_cell_background)
                button.scaleType = ImageView.ScaleType.CENTER_INSIDE
                button.setPadding(16, 16, 16, 16)

                button.setOnClickListener {
                    onCellClick(row, col)
                }

                gameButtons[row][col] = button
                gameBoard.addView(button)
            }
        }
    }

    private fun onCellClick(row: Int, col: Int) {
        val gameData = gameModel.getCurrentGameData()

        if (!gameData.isGameActive) return

        if (gameModel.makeMove(row, col)) {
            updateUI()

            val newGameData = gameModel.getCurrentGameData()
            when {
                newGameData.winner != null -> {
                    Toast.makeText(this, "${newGameData.winner.displayName} wins!", Toast.LENGTH_LONG).show()
                    highlightWinningCells()
                }
                newGameData.isDraw -> {
                    Toast.makeText(this, "Game ended in a draw!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUI() {
        val gameData = gameModel.getCurrentGameData()

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val button = gameButtons[row][col]
                val cellValue = gameData.boardState[row][col]

                when (cellValue) {
                    Player.ICE.name -> {
                        button?.setImageResource(Player.ICE.logoRes)
                        button?.setBackgroundResource(R.drawable.game_cell_selected)
                    }
                    Player.FIRE.name -> {
                        button?.setImageResource(Player.FIRE.logoRes)
                        button?.setBackgroundResource(R.drawable.game_cell_selected)
                    }
                    else -> {
                        button?.setImageDrawable(null)
                        button?.setBackgroundResource(R.drawable.game_cell_background)
                    }
                }
            }
        }

        when {
            gameData.winner != null -> {
                currentPlayerText.text = "${gameData.winner.displayName} Wins!"
                currentPlayerText.setTextColor(Color.YELLOW)
            }
            gameData.isDraw -> {
                currentPlayerText.text = "It's a Draw!"
                currentPlayerText.setTextColor(Color.GRAY)
            }
            else -> {
                currentPlayerText.text = "${gameData.currentPlayer.displayName}'s Turn"
                currentPlayerText.setTextColor(Color.WHITE)
            }
        }
    }

    private fun highlightWinningCells() {
        val winningPositions = gameModel.getWinningPositions()
        winningPositions?.forEach { (row, col) ->
            gameButtons[row][col]?.setBackgroundResource(R.drawable.game_cell_winner)
        }
    }

    private fun setupClickListeners() {
        resetButton.setOnClickListener {
            resetGame()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun resetGame() {
        gameModel.resetGame()
        updateUI()
        Toast.makeText(this, "New game started!", Toast.LENGTH_SHORT).show()
    }
}
