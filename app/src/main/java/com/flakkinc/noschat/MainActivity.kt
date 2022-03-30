package com.flakkinc.noschat

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
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
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.squareup.picasso.Picasso
import org.json.JSONObject

@IgnoreExtraProperties
data class UserData(
    val displayName: String = "",
    val lastServer: String = "",
    val profPic: String = "",
    val profPage: Any = {},
    val servers: List<String> = listOf()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "displayName" to displayName,
            "lastServer" to lastServer,
            "profPic" to profPic,
            "profPage" to profPage,
            "servers" to servers
        )
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val user = auth.currentUser
        var userData: UserData

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
            database.child("users").child(user.uid).get().addOnSuccessListener { it ->
                if(it.exists()) {
                    userData = it.getValue(UserData::class.java) as UserData
                    updateMainDisplay(userData.lastServer)
                    for (id in userData.servers) {
                        database.child("servers").child(id).child("name").get().addOnSuccessListener { it1 ->
                            if(it1.exists()) {
                                var info: String = it1.value.toString()
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

                                temp.setOnClickListener { view ->
                                    database.child("users").child(user.uid).child("lastServer").setValue(id)
                                    database.child("users").child(user.uid).get().addOnSuccessListener { it ->
                                        if (it.exists()) {
                                            userData = it.getValue(UserData::class.java) as UserData
                                            updateMainDisplay(userData.lastServer)
                                        } else {
                                            Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                serverMenu.addView(temp)

                            } else {
                                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Unable to fetch User Data. Please Login Again", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.joinServerButton.setOnClickListener {view ->
            val builder = AlertDialog.Builder(this)

            val input = EditText(baseContext)
            input.hint = "Server Code..."
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView((input))

            builder.apply {
                setPositiveButton("Join",
                    DialogInterface.OnClickListener {dialog, id ->
                        var inputtedId = input.text.toString()
                        database.child("servers").child(inputtedId).get().addOnSuccessListener {
                            if(it.exists()) {
                                database.child("servers").child(inputtedId).child("members").get().addOnSuccessListener { it2 ->
                                    if(it2.exists()) {
                                        var data: List<String>? = it2.value as List<String>
                                        data?.plus(auth.uid)
                                        database.child("servers").child(inputtedId).child("members").setValue(data)
                                        database.child("users").child(auth.uid.toString()).child("servers").get().addOnSuccessListener { it3 ->
                                            if(it3.exists()) {
                                                var data: List<String>? = it3.value as List<String>
                                                data?.plus(auth.uid)
                                                database.child("users").child(auth.uid.toString()).child("servers").setValue(data)

                                            }
                                        }

                                    }
                                }
                            } else {
                                Toast.makeText(baseContext, "Server Id Is Invalid Or Does Not Exist", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener{dialog, id ->

                    }
                )
            }
            builder.setTitle("Enter Server Id Below To Join")
            builder.create().show()
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

    private fun updateMainDisplay(id: String) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val serverMenuContainer = binding.serverMenuContainer
        val sideMenu = binding.sideNav
        val mainDisplay = binding.lister

        serverMenuContainer.visibility = View.INVISIBLE
        sideMenu.visibility = View.INVISIBLE

        val temp = TextView(baseContext)
        temp.text = id
        mainDisplay.addView(temp)
    }
}

fun Int.dpToPixels(context: Context):Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
fun Float.dpToPixels(context: Context):Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)