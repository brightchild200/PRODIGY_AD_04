package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUpPrompt: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)   // make sure your XML file is named this way

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        usernameEdit  = findViewById(R.id.usernameEdit)
        passwordEdit  = findViewById(R.id.passwordEdit)
        loginBtn      = findViewById(R.id.loginBtn)
        signUpPrompt  = findViewById(R.id.signuptext)  // ← set android:id in XML, see note below
        progressBar   = ProgressBar(this).apply { visibility = View.GONE }

        loginBtn.setOnClickListener { attemptLogin() }
        signUpPrompt.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<ImageView>(R.id.backBtn).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun attemptLogin() {
        val userInput = usernameEdit.text.toString().trim()
        val password  = passwordEdit.text.toString().trim()

        if (userInput.isEmpty() || password.isEmpty()) {
            toast("Please enter both fields")
            return
        }

        progressBar.visibility = View.VISIBLE
        if (userInput.contains("@")) {
            // Treat as email directly
            signInWithEmail(userInput, password)
        } else {
            // Treat as username → look up corresponding email in Firestore
            db.collection("users")
                .whereEqualTo("username", userInput)
                .limit(1)
                .get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) {
                        progressBar.visibility = View.GONE
                        toast("Username not found")
                    } else {
                        val email = snap.documents[0].getString("email") ?: ""
                        signInWithEmail(email, password)
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    toast("Error: ${it.message}")
                }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                toast("Logged in!")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                toast("Login failed: ${it.message}")
            }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
