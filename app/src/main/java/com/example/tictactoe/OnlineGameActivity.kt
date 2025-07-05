// OnlineGameActivity.kt  – updated with Firebase win/loss/draw stat tracking
// (only online games update stats; offline remains unaffected)

package com.example.tictactoe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class OnlineGameActivity : AppCompatActivity() {

    // ── Firebase ────────────────────────────────────────────────────────
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }
    private lateinit var gameRef: DocumentReference
    private lateinit var myUid: String
    private var amHost = false

    // ── UI refs ─────────────────────────────────────────────────────────
    private lateinit var backBtn      : ImageView
    private lateinit var profileIcon  : ImageView
    private lateinit var gameCodeText : TextView
    private lateinit var waitingText  : TextView
    private lateinit var currentTxt   : TextView
    private lateinit var resetBtn     : Button
    private lateinit var boardGrid    : GridLayout
    private lateinit var p1NameTxt    : TextView
    private lateinit var p2NameTxt    : TextView
    private val cells = Array(3) { arrayOfNulls<ImageView>(3) }

    // ── Local game vars ────────────────────────────────────────────────
    private var mySymbol = ""            // ICE / FIRE
    private var gameCode = ""
    private var board    = MutableList(9){""}
    private var status   = "waiting"      // waiting / started / finished
    private var statsRecorded = false     // ensure we write stats only once

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.online_game)

        bindViews()
        myUid = auth.currentUser?.uid ?: return finish()

        // Determine host vs guest
        intent.getStringExtra("GAME_CODE")?.let { code ->
            gameCode = code
            mySymbol = "FIRE"
            joinGame()
        } ?: run {
            amHost = true
            mySymbol = "ICE"
            gameCode = generateCode()
            createGame()
            showShareDialog()
        }

        setupBoardUI()
        listenForChanges()
        setClickListeners()
    }

    // ── Binding UI ──────────────────────────────────────────────────────
    private fun bindViews() {
        backBtn      = findViewById(R.id.back_button)
        profileIcon  = findViewById(R.id.player1Avatar)
        gameCodeText = findViewById(R.id.gameCodeText)
        waitingText  = findViewById(R.id.waitingText)
        currentTxt   = findViewById(R.id.currentPlayerText)
        resetBtn     = findViewById(R.id.resetButton)
        boardGrid    = findViewById(R.id.gameBoard)
        p1NameTxt    = findViewById(R.id.player1Name)
        p2NameTxt    = findViewById(R.id.player2Name)
    }

    private fun assertViews() {
        listOf(backBtn, profileIcon, gameCodeText, waitingText,
            currentTxt, resetBtn, boardGrid, p1NameTxt, p2NameTxt).forEach {
            requireNotNull(it) { "View binding failed for ${it}" }
        }
    }


    // ── Create / Join game ──────────────────────────────────────────────
    private fun createGame() {
        gameCodeText.text = "Code: $gameCode"
        val meDoc = db.collection("users").document(myUid)
        meDoc.get().addOnSuccessListener { meSnap ->
            val meName   = meSnap.getString("username") ?: "Me"
            val meAvatar = meSnap.getLong("avatarResId")?.toInt() ?: R.drawable.avatar1
            profileIcon.setImageResource(meAvatar)
            p1NameTxt.text = meName

            val data = hashMapOf(
                "status"        to "waiting",
                "board"         to board,
                "turn"          to mySymbol,
                "hostUid"       to myUid,
                "hostName"      to meName,
                "hostAvatarRes" to meAvatar,
                "guestUid"      to null,
                "winner"        to null
            )
            gameRef = db.collection("games").document(gameCode)
            gameRef.set(data)
        }
    }

    private fun joinGame() {
        gameCodeText.text = "Code: $gameCode"
        gameRef = db.collection("games").document(gameCode)
        val meDoc = db.collection("users").document(myUid)
        meDoc.get().addOnSuccessListener { meSnap ->
            val meName   = meSnap.getString("username") ?: "Me"
            val meAvatar = meSnap.getLong("avatarResId")?.toInt() ?: R.drawable.avatar1
            profileIcon.setImageResource(meAvatar)
            p2NameTxt.text = meName

            db.runTransaction { tx ->
                val snap = tx.get(gameRef)
                if (snap.getString("guestUid") == null) {
                    tx.update(gameRef, mapOf(
                        "guestUid"       to myUid,
                        "guestName"      to meName,
                        "guestAvatarRes" to meAvatar,
                        "status"         to "started"
                    ))
                }
            }.addOnFailureListener { toast("Room full"); finish() }
        }
    }

    // ── Listener ───────────────────────────────────────────────────────
    private fun listenForChanges() {
        gameRef.addSnapshotListener { snap, err ->
            if (err != null || snap == null || !snap.exists()) { toast("Game deleted"); finish(); return@addSnapshotListener }
            status = snap.getString("status") ?: "waiting"
            board  = (snap["board"] as List<String>).toMutableList()

            updateNamesAndAvatars(snap)
            updateBoardUI()
            updateTurnText(snap)

            // ── Record stats once, when game finishes ──
            if (status == "finished" && !statsRecorded) {
                val winner = snap.getString("winner")
                val result = when {
                    winner == null -> "DRAW"
                    winner == mySymbol -> "WIN"
                    else -> "LOSS"
                }
                updateUserStats(result)
                statsRecorded = true
            }
        }
    }

    private fun updateNamesAndAvatars(snap: DocumentSnapshot) {
        p1NameTxt.text = snap.getString("hostName") ?: "Player 1"
        p2NameTxt.text = snap.getString("guestName") ?: "Player 2"
        snap.getLong("hostAvatarRes")?.toInt()?.let { findViewById<ImageView>(R.id.player1Avatar).setImageResource(it) }
        snap.getLong("guestAvatarRes")?.toInt()?.let { findViewById<ImageView>(R.id.player2Avatar).setImageResource(it) }
    }

    // ── UI helpers ─────────────────────────────────────────────────────
    private fun setupBoardUI() {
        boardGrid.removeAllViews()
        for (r in 0..2) for (c in 0..2) {
            val iv = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width  = 0; height = 0
                    columnSpec = GridLayout.spec(c, 1f)
                    rowSpec    = GridLayout.spec(r, 1f)
                    setMargins(4,4,4,4)
                }
                setBackgroundResource(R.drawable.game_cell_background)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(16,16,16,16)
                setOnClickListener { onCellClick(r,c) }
            }
            cells[r][c] = iv
            boardGrid.addView(iv)
        }
    }

    private fun updateBoardUI() {
        for (r in 0..2) for (c in 0..2) {
            when (board[r*3+c]) {
                "ICE"  -> cells[r][c]?.setImageResource(Player.ICE.logoRes)
                "FIRE" -> cells[r][c]?.setImageResource(Player.FIRE.logoRes)
                else    -> cells[r][c]?.setImageDrawable(null)
            }
        }
    }

    private fun updateTurnText(snap: DocumentSnapshot) {
        waitingText.visibility = if (status=="waiting") TextView.VISIBLE else TextView.GONE
        val winner = snap.getString("winner")
        val turn   = snap.getString("turn")
        when {
            winner != null -> { currentTxt.text = "$winner wins!"; currentTxt.setTextColor(Color.YELLOW) }
            turn == mySymbol -> { currentTxt.text = "Your turn"; currentTxt.setTextColor(Color.WHITE) }
            else -> { currentTxt.text = "Opponent's turn"; currentTxt.setTextColor(Color.GRAY) }
        }
    }

    // ── Cell click ─────────────────────────────────────────────────────
    private fun onCellClick(r:Int,c:Int) {
        if (status != "started") return
        val idx = r*3+c
        if (board[idx].isNotEmpty()) return

        db.runTransaction { transaction ->
            val snap = transaction.get(gameRef)
            if (snap.getString("turn") != mySymbol) return@runTransaction
            val remoteBoard = (snap["board"] as List<String>).toMutableList()
            if (remoteBoard[idx].isNotEmpty()) return@runTransaction
            remoteBoard[idx] = mySymbol
            val winner = checkWinner(remoteBoard)
            transaction.update(gameRef, mapOf(
                "board" to remoteBoard,
                "turn"  to if (mySymbol=="ICE") "FIRE" else "ICE",
                "winner" to winner,
                "status" to if (winner!=null || remoteBoard.none { it.isEmpty() }) "finished" else "started"
            ))
        }
    }

    private fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0,1,2), listOf(3,4,5), listOf(6,7,8),
            listOf(0,3,6), listOf(1,4,7), listOf(2,5,8),
            listOf(0,4,8), listOf(2,4,6)
        )
        for (l in lines) if (b[l[0]].isNotEmpty() && b[l[0]]==b[l[1]] && b[l[0]]==b[l[2]]) return b[l[0]]
        return null
    }

    // ── User stats update ─────────────────────────────────────────────
    private fun updateUserStats(result:String) {
        val ref = db.collection("users").document(myUid)
        val updates = hashMapOf<String,Any>("gamesPlayed" to FieldValue.increment(1))
        when(result) {
            "WIN"  -> updates["wins"]   = FieldValue.increment(1)
            "LOSS" -> updates["losses"] = FieldValue.increment(1)
            "DRAW" -> updates["draws"]  = FieldValue.increment(1)
        }
        ref.set(updates, SetOptions.merge())
    }

    // ── Share dialog, clicks, helpers ──────────────────────────────────
    private fun showShareDialog() {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_share_code, null)
        v.findViewById<TextView>(R.id.codeTextView).text = gameCode
        val dlg = AlertDialog.Builder(this).setView(v).setCancelable(false).create()
        dlg.show()

        val inviteMsg = "Join my Tic Tac Toe game! Code: $gameCode"
        v.findViewById<Button>(R.id.copyBtn).setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Game Code", gameCode))
            toast("Copied")
        }
        v.findViewById<Button>(R.id.shareBtn).setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type="text/plain"; i.putExtra(Intent.EXTRA_TEXT, inviteMsg)
            startActivity(Intent.createChooser(i,"Share via"))
        }

        gameRef.addSnapshotListener { snap,_ -> if (snap?.getString("status") == "started") dlg.dismiss() }
    }

    private fun setClickListeners() {
        backBtn.setOnClickListener { finish() }
        resetBtn.setOnClickListener { if(amHost) resetGame() else toast("Only host can reset") }
        profileIcon.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    private fun resetGame() {
        board = MutableList(9){""}
        gameRef.update(mapOf("board" to board,"turn" to "ICE","winner" to null,"status" to "started"))
        statsRecorded = false
    }

    private fun generateCode(): String = (100000..999999).random().toString()
    private fun toast(msg:String)=Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}
