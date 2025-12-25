package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class UserHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        val btnLogout: Button = findViewById(R.id.btnLogout)

        val i: Intent = intent
        val isAgent: Boolean = i.getBooleanExtra("USER_AGENT", false)
        val user = i.getParcelableExtra<User>("USER")

        if (user != null) {
            val welcomeText = findViewById<TextView>(R.id.welcomeText)
            welcomeText.text = "Добро пожаловать, ${user.login}!"

            if (isAgent) {
                loadFragment(AdminFragment(user))
            } else {
                loadFragment(MainClientFragment(user))
            }
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}