package com.flakkinc.noschat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.flakkinc.noschat.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            login(username.text.toString(), password.text.toString())
        }

        if(auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }
    }

    private fun login(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(baseContext, "Auth Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}