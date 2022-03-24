package com.flakkinc.noschat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.flakkinc.noschat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.squareup.picasso.Picasso
import kotlin.reflect.typeOf


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val user = auth.currentUser

        database = Firebase.database.reference

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serverMenu = binding.serverMenu

        if(user === null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        } else {
            database.child("users").child(user.uid).child("servers").get().addOnSuccessListener {
                if(it.exists()){
                    var serverIds: List<String>? = it.value as List<String>?
                    serverIds?.let {
                        for (id in serverIds) {
                            val temp = CardView(this)
                            val params = RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            temp.layoutParams = params
                            val temp2 = TextView(this)
                            temp2.text = id
                            temp.addView(temp2)
                            serverMenu.addView(temp)
                        }
                    }

                } else {
                    Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                }

            }
        }

        //setSupportActionBar(binding.toolbar)

        /*val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)*/

        binding.menuButton.setOnClickListener {view ->
            if(binding.sideNav.visibility == View.VISIBLE) {
                binding.sideNav.visibility = View.GONE
            } else {
                binding.sideNav.visibility = View.VISIBLE
                serverMenu.visibility = View.GONE
            }
        }

        binding.serverButton.setOnClickListener {view ->
            if(serverMenu.visibility == View.VISIBLE) {
                serverMenu.visibility = View.GONE
            } else {
                serverMenu.visibility = View.VISIBLE
                binding.sideNav.visibility = View.GONE
            }
        }

        binding.logoutButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setPositiveButton("Yes",
                    DialogInterface.OnClickListener {dialog, id ->
                            auth.signOut()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }
                    )
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener{dialog, id ->

                    }
                )
            }
            builder.setTitle("Are Your Sure You Want To Logout?")
            builder.create().show()
        }



        val imageView = binding.profPic
        val imageURL = user?.photoUrl.toString()

        Picasso.get().load(imageURL).into(imageView)
    }
}