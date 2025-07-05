package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var playOfflineButton: Button
    private lateinit var playOnlineButton: Button
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    private lateinit var profilesection: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        playOfflineButton = findViewById(R.id.play_offline)
        playOnlineButton  = findViewById(R.id.play_online)
        signupButton = findViewById(R.id.signup_btn)
        loginButton = findViewById(R.id.login_btn)
        profilesection = findViewById(R.id.profilesection)
    }

    private fun setupClickListeners() {

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }

        // 1️⃣  “Play Offline” → GameActivity (no auth check)
        playOfflineButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        profilesection.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 2️⃣  “Play Online” → OnlineGameActivity, but only if logged in
//        playOnlineButton.setOnClickListener {
//            if (auth.currentUser != null) {
//                startActivity(Intent(this, OnlineGameActivity::class.java))
//            } else {
//                Toast.makeText(this, "Please log in first!", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, LoginActivity::class.java))
//            }
//        }
    }

}
