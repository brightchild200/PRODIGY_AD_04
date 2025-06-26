package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var avatarGrid: GridLayout
    private var selectedAvatarId: Int = R.drawable.avatar1   // default avatar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var loginPrompt: TextView               // ← only ONE declaration now
    private lateinit var backButton: ImageView

    private val avatarRes = listOf(
        R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3,
        R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
        R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9,
        R.drawable.avatar10, R.drawable.avatar11, R.drawable.avatar12,
        R.drawable.avatar13, R.drawable.avatar14, R.drawable.avatar15,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        avatarGrid   = findViewById(R.id.avatarGrid)
        val usernameEdit = findViewById<EditText>(R.id.usernameEdit)
        val emailEdit    = findViewById<EditText>(R.id.emailEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val signupBtn    = findViewById<Button>(R.id.signupBtn)
        backButton = findViewById(R.id.backBtn)
        loginPrompt      = findViewById(R.id.logintext)      // ← bind the view

        /* ─── 1. Avatar grid ───────────────────────────────────────────── */
        avatarRes.forEach { resId ->
            val avatarView = ImageView(this).apply {
                setImageResource(resId)
                layoutParams = ViewGroup.LayoutParams(140, 140)
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    selectedAvatarId = resId
                    highlightSelectedAvatar(this)
                }
            }
            avatarGrid.addView(avatarView)
        }

        /* ─── 2. Sign-up button ─────────────────────────────────────────── */
        signupBtn.setOnClickListener {
            val username = usernameEdit.text.toString().trim()
            val email    = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val user = hashMapOf(
                        "uid"          to uid,
                        "username"     to username,
                        "email"        to email,
                        "avatarResId"  to selectedAvatarId
                    )
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signed up!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        /* ─── 3. “Login instead” prompt ─────────────────────────────────── */
        loginPrompt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun highlightSelectedAvatar(selected: ImageView) {
        for (i in 0 until avatarGrid.childCount) {
            (avatarGrid.getChildAt(i) as ImageView).setBackgroundResource(0)
        }
        selected.setBackgroundResource(R.drawable.avatar_selected_border)
    }
}
