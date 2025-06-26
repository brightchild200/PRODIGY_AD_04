// OnlineGameActivity.kt
package com.example.tictactoe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var gameBoard: GridLayout
    private lateinit var currentPlayerText: TextView
    private lateinit var resetButton: Button
    private lateinit var gameCodeText: TextView
    private lateinit var waitingText: TextView

    private val boardSize = 3
    private val gameButtons = Array(boardSize) { Array<ImageView?>(boardSize) { null } }

    private lateinit var database: DatabaseReference
    private var gameId: String = ""
    private var playerId: String = ""
    private var playerNumber: Int = 0 // 1 or 2
    private var isMyTurn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.online_game)

        // Initialize Firebase
        database = Firebase.database.reference

        initializeViews()
        setupGameBoard()

        // Generate unique IDs
        gameId = generateGameId()
        playerId = generatePlayerId()

        // Join or create game
        joinOrCreateGame()
    }

    private fun initializeViews() {
        gameBoard = findViewById(R.id.gameBoard)
        currentPlayerText = findViewById(R.id.currentPlayerText)
        resetButton = findViewById(R.id.resetButton)
        gameCodeText = findViewById(R.id.gameCodeText)
        waitingText = findViewById(R.id.waitingText)

        // Add back button functionality
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            leaveGame()
            finish()
        }

        resetButton.setOnClickListener {
            // Leave current game and create new one
            leaveGame()
            gameId = generateGameId()
            gameCodeText.text = "Game Code: $gameId"
            joinOrCreateGame()
        }
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
                    if (isMyTurn) {
                        makeMove(row, col)
                    } else {
                        Toast.makeText(this, "Wait for your turn!", Toast.LENGTH_SHORT).show()
                    }
                }

                gameButtons[row][col] = button
                gameBoard.addView(button)
            }
        }
    }

    private fun joinOrCreateGame() {
        val gameRef = database.child("games").child(gameId)

        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Game exists, join as player 2
                    playerNumber = 2
                    isMyTurn = false
                    joinExistingGame(gameRef)
                } else {
                    // Create new game as player 1
                    playerNumber = 1
                    isMyTurn = true
                    createNewGame(gameRef)
                }
                setupGameListener(gameRef)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineGameActivity, "Failed to join game", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createNewGame(gameRef: DatabaseReference) {
        val gameData = mapOf(
            "player1" to mapOf(
                "id" to playerId,
                "name" to "Player 1"
            ),
            "player2" to mapOf(
                "id" to "",
                "name" to ""
            ),
            "currentTurn" to 1,
            "board" to List(3) { List(3) { "" } },
            "gameStatus" to "waiting",
            "winner" to "",
            "gameCode" to gameId
        )

        gameRef.setValue(gameData)

        gameCodeText.text = "Game Code: $gameId"
        waitingText.text = "Share this code with your friend!\nWaiting for another player..."
        waitingText.visibility = TextView.VISIBLE
        disableGameBoard(true)
    }


    private fun joinExistingGame(gameRef: DatabaseReference) {
        val playerData = mapOf(
            "id" to playerId,
            "name" to "Player 2"
        )

        gameRef.child("player2").setValue(playerData)
        gameRef.child("gameStatus").setValue("active")
        waitingText.visibility = TextView.GONE
        disableGameBoard(false)
    }


    private fun setupGameListener(gameRef: DatabaseReference) {
        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    updateGameState(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineGameActivity, "Connection lost", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGameState(snapshot: DataSnapshot) {
        val gameStatus = snapshot.child("gameStatus").getValue(String::class.java) ?: ""
        val currentTurn = snapshot.child("currentTurn").getValue(Int::class.java) ?: 1
        val board = snapshot.child("board").getValue() as? List<List<String>> ?: return
        val winner = snapshot.child("winner").getValue(String::class.java) ?: ""

        // Update player cards
        updatePlayerCards(snapshot)

        // Update board UI
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val cellValue = board[row][col]
                val button = gameButtons[row][col]

                when (cellValue) {
                    "ICE" -> {
                        button?.setImageResource(R.drawable.ice)
                        button?.setBackgroundResource(R.drawable.game_cell_selected)
                    }
                    "FIRE" -> {
                        button?.setImageResource(R.drawable.fire)
                        button?.setBackgroundResource(R.drawable.game_cell_selected)
                    }
                    else -> {
                        button?.setImageDrawable(null)
                        button?.setBackgroundResource(R.drawable.game_cell_background)
                    }
                }
            }
        }

        // Update game status
        when (gameStatus) {
            "waiting" -> {
                waitingText.visibility = TextView.VISIBLE
                waitingText.text = "Share code: $gameId\nWaiting for another player..."
                currentPlayerText.text = ""
                disableGameBoard(true)
            }
            "active" -> {
                waitingText.visibility = TextView.GONE
                disableGameBoard(false)
                isMyTurn = currentTurn == playerNumber

                if (winner.isNotEmpty()) {
                    val winnerText = if (winner == "ICE") "Player 1 (Ice)" else "Player 2 (Fire)"
                    currentPlayerText.text = "$winnerText Wins!"
                    isMyTurn = false
                    disableGameBoard(true)
                } else {
                    val playerName = if (currentTurn == 1) "Player 1 (Ice)" else "Player 2 (Fire)"
                    currentPlayerText.text = if (isMyTurn) "Your Turn" else "$playerName's Turn"
                }
            }
        }
    }

    private fun makeMove(row: Int, col: Int) {
        val gameRef = database.child("games").child(gameId)

        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val board = snapshot.child("board").getValue() as? MutableList<MutableList<String>> ?: return

                // Check if cell is empty
                if (board[row][col].isNotEmpty()) {
                    Toast.makeText(this@OnlineGameActivity, "Cell already taken!", Toast.LENGTH_SHORT).show()
                    return
                }

                // Make move
                val playerSymbol = if (playerNumber == 1) "ICE" else "FIRE"
                board[row][col] = playerSymbol

                // Check for winner
                val winner = checkWinner(board, playerSymbol)
                val nextTurn = if (playerNumber == 1) 2 else 1

                // Update database
                val updates = mutableMapOf<String, Any>()
                updates["board"] = board
                updates["currentTurn"] = nextTurn
                if (winner != null) {
                    updates["winner"] = winner
                }

                gameRef.updateChildren(updates)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OnlineGameActivity, "Failed to make move", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkWinner(board: List<List<String>>, player: String): String? {
        // Check rows
        for (row in 0..2) {
            if (board[row].all { it == player }) return player
        }

        // Check columns
        for (col in 0..2) {
            if ((0..2).all { row -> board[row][col] == player }) return player
        }

        // Check diagonals
        if ((0..2).all { i -> board[i][i] == player } ||
            (0..2).all { i -> board[i][2-i] == player }) {
            return player
        }

        return null
    }

    private fun leaveGame() {
        if (gameId.isNotEmpty()) {
            database.child("games").child(gameId).removeValue()
        }
    }

    private fun generateGameId(): String {
        return (100000..999999).random().toString()
    }

    private fun generatePlayerId(): String {
        return System.currentTimeMillis().toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveGame()
    }

    private fun updatePlayerCards(snapshot: DataSnapshot) {
        val player1Data = snapshot.child("player1").getValue() as? Map<String, String>
        val player2Data = snapshot.child("player2").getValue() as? Map<String, String>

        findViewById<TextView>(R.id.player1Name)?.text = player1Data?.get("name") ?: "Player 1"
        findViewById<TextView>(R.id.player2Name)?.text = player2Data?.get("name") ?: "Waiting..."
    }


//    private fun getRandomAvatar(): String {
//        val avatars = listOf("avatar1", "avatar2", "avatar3", "avatar4", "avatar5")
//        return avatars.random()
//    }
//
//    private fun getAvatarResource(avatarName: String): Int {
//        return when (avatarName) {
//            "avatar1" -> R.drawable.avatar1
//            "avatar2" -> R.drawable.avatar2
//            "avatar3" -> R.drawable.avatar3
//            "avatar4" -> R.drawable.avatar4
//            "avatar5" -> R.drawable.avatar5
//            else -> R.drawable.player_card_background
//        }
//    }

    private fun disableGameBoard(disable: Boolean) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                gameButtons[row][col]?.isEnabled = !disable
                if (disable) {
                    gameButtons[row][col]?.alpha = 0.5f
                } else {
                    gameButtons[row][col]?.alpha = 1.0f
                }
            }
        }
    }
}