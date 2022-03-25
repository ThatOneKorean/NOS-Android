package com.flakkinc.noschat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.flakkinc.noschat.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import java.lang.Exception

data class User(
    val displayName:String? = null,
    val profPic:String? = "https://firebasestorage.googleapis.com/v0/b/nos-version-3.appspot.com/o/default.png?alt=media&token=cb92c2f1-db93-4296-87ba-fefe019b8092",
    val servers:List<String>? = listOf("-MtpJ3eHyL_jvcGXbXE7")
)

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database.reference

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val regLink = binding.registerLink
        val loginLink = binding.loginLink
        val regDisp = binding.signupContainer
        val logDisp = binding.loginContainer
        val regButton = binding.signupButton

        if(auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }

        login.setOnClickListener {
            login(username.text.toString(), password.text.toString())
        }

        regLink.setOnClickListener {
            regDisp.visibility = View.VISIBLE
            logDisp.visibility = View.GONE
        }

        loginLink.setOnClickListener {
            logDisp.visibility = View.VISIBLE
            regDisp.visibility = View.GONE
        }

        regButton.setOnClickListener {
            signup(binding.signupDisplayName.text.toString(), binding.signupPassword.text.toString(), binding.signupConfirmPassword.text.toString(), binding.signupEmail.text.toString())
        }
    }

    private fun login(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener {task ->
                val user = auth.currentUser
                if (task.isSuccessful && user != null) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(baseContext, "Auth Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signup(displayNames: String, password1: String, password2: String, email: String) {
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            if(password1 == password2) {
                auth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(this) { task ->
                    val user = auth.currentUser
                    if(task.isSuccessful && user != null) {
                        user.updateProfile(userProfileChangeRequest {
                            displayName = displayNames
                            photoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/nos-version-3.appspot.com/o/default.png?alt=media&token=cb92c2f1-db93-4296-87ba-fefe019b8092")
                        }).addOnSuccessListener {
                            database.child("users").child(user.uid).setValue(User(displayNames)).addOnSuccessListener {
                                auth.signOut()
                                binding.loginContainer.visibility = View.VISIBLE
                                binding.signupContainer.visibility = View.GONE
                                Toast.makeText(baseContext, "Please Sign In To Continue", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(baseContext, task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(baseContext, "Passwords Do Not Match!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}