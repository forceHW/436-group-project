package com.example.groupproject

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Logins : AppCompatActivity(){
    lateinit var textView: TextView
    lateinit var logoutButton : Button
    lateinit var changeButton : Button
    lateinit var backButton: Button

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res -> this.onSignInResult(res)}

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                textView.text = "Logged in as: " + user.displayName
                logoutButton.isEnabled = true

            }
        }
    }

    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build())

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        var auth : FirebaseAuth = FirebaseAuth.getInstance()
        var user : FirebaseUser? = auth.currentUser
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.FirebaseAuthUI)
            .setAvailableProviders(providers)
            .build()
        setContentView(R.layout.login_view)
        textView = findViewById(R.id.textView3)
        logoutButton = findViewById(R.id.button2)
        logoutButton.setOnClickListener { AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                textView.text = "Logged out successfully"
            } }
        changeButton = findViewById(R.id.button3)

        if (user != null) {
            textView.text = "Logged in as: " + user.displayName
            logoutButton.isEnabled = true
        }else{
            textView.text = "Not Logged in"
            logoutButton.isEnabled = false
        }

        changeButton.setOnClickListener { signInLauncher.launch(signInIntent) }

        backButton = findViewById(R.id.button4)

        backButton.setOnClickListener { finish() }





    }
}