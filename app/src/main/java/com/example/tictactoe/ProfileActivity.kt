package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var avatarImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var totalGamesText: TextView
    private lateinit var winsText: TextView
    private lateinit var lossesText: TextView
    private lateinit var drawsText: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: ImageView

    private lateinit var changeUsernameLayout: LinearLayout
    private lateinit var changeAvatarLayout: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        initializeViews()
        loadUserData()

        backButton.setOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        changeUsernameLayout.setOnClickListener {
            Toast.makeText(this, "Coming soon: Change Username", Toast.LENGTH_SHORT).show()
        }

        changeAvatarLayout.setOnClickListener {
            Toast.makeText(this, "Coming soon: Change Avatar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        avatarImage = findViewById(R.id.iv_avatar)
        usernameText = findViewById(R.id.tv_username)
        emailText = findViewById(R.id.tv_email)
        totalGamesText = findViewById(R.id.tv_total_games)
        winsText = findViewById(R.id.tv_wins)
        lossesText = findViewById(R.id.tv_losses)
        drawsText = findViewById(R.id.tv_draws)
        logoutButton = findViewById(R.id.btn_logout)
        changeUsernameLayout = findViewById(R.id.ll_change_username)
        changeAvatarLayout = findViewById(R.id.ll_change_avatar)
        backButton = findViewById(R.id.back_button)
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val username = doc.getString("username") ?: "Unknown"
                val email = doc.getString("email") ?: ""
                val avatarResId = doc.getLong("avatarResId")?.toInt() ?: R.drawable.avatar1
                val gamesPlayed = doc.getLong("gamesPlayed") ?: 0
                val wins = doc.getLong("wins") ?: 0
                val losses = doc.getLong("losses") ?: 0
                val draws = doc.getLong("draws") ?: 0

                usernameText.text = username
                emailText.text = email
                avatarImage.setImageResource(avatarResId)
                totalGamesText.text = gamesPlayed.toString()
                winsText.text = wins.toString()
                lossesText.text = losses.toString()
                drawsText.text = draws.toString()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }
}
