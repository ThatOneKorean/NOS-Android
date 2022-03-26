package com.flakkinc.noschat

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.flakkinc.noschat.databinding.ActivityMainBinding
import com.google.android.gms.common.internal.Objects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.squareup.picasso.Picasso
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

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
        val serverMenuContainer = binding.serverMenuContainer
        val sideMenu = binding.sideNav
        val settingsMenu = binding.SettingsLayout
        val settingsButton = binding.openSettingsButton
        val closeSettingsButton = binding.closeSettingsMenuButton

        if(user === null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        } else {
            database.child("users").child(user.uid).child("servers").get().addOnSuccessListener {
                if(it.exists()){
                    var serverIds: List<String>? = it.value as List<String>?
                    serverIds?.let {
                        for (id in serverIds) {
                            database.child("servers").child(id).child("name").get().addOnSuccessListener { it ->
                                if(it.exists()) {
                                    var info: String = it.value as String
                                    val temp = LinearLayout(this)
                                    temp.orientation = LinearLayout.HORIZONTAL
                                    var params = RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    temp.layoutParams = params

                                    val serverPicContainer = CardView(this)

                                    params = RelativeLayout.LayoutParams(50.dpToPixels(this), 50.dpToPixels(this))
                                    serverPicContainer.layoutParams = params
                                    serverPicContainer.radius = 37.5f.dpToPixels(this)

                                    val serverPic = ImageView(this)

                                    database.child("servers").child(id).child("serverPic").get().addOnSuccessListener { it2 ->
                                        if(it2.exists()) {
                                            Picasso.get().load(it2.value.toString()).into(serverPic)
                                        }
                                    }

                                    serverPicContainer.addView(serverPic)
                                    temp.addView(serverPicContainer)

                                    val temp2 = TextView(this)
                                    temp2.text = info
                                    temp.addView(temp2)
                                    serverMenu.addView(temp)
                                }
                            }
                        }
                    }

                } else {
                    Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                }

            }
        }

        binding.menuButton.setOnClickListener {view ->
            if(sideMenu.visibility == View.VISIBLE) {
                sideMenu.visibility = View.INVISIBLE
            } else {
                sideMenu.visibility = View.VISIBLE
                serverMenuContainer.visibility = View.INVISIBLE
            }
        }

        binding.serverButton.setOnClickListener {view ->
            if(serverMenuContainer.visibility == View.VISIBLE) {
                serverMenuContainer.visibility = View.INVISIBLE
            } else {
                serverMenuContainer.visibility = View.VISIBLE
                sideMenu.visibility = View.INVISIBLE
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

        settingsButton.setOnClickListener {view ->
            settingsMenu.visibility = View.VISIBLE
            sideMenu.visibility = View.INVISIBLE
        }

        closeSettingsButton.setOnClickListener {view ->
            settingsMenu.visibility = View.GONE
        }

        val imageView = binding.profPic
        val imageURL = user?.photoUrl.toString()

        Picasso.get().load(imageURL).into(imageView)
    }
}

fun Int.dpToPixels(context: Context):Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
fun Float.dpToPixels(context: Context):Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)