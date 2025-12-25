package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Создание тестовых пользователей при первом запуске
        createTestUsers()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createTestUsers() {
        Thread {
            try {
                val db = MainDB.getDb(this)

                // Проверяем, есть ли уже тестовый пользователь
                val existingUser = db.getUserDao().getUserByLogin("testuser")
                val existingAgent = db.getUserDao().getUserByLogin("testagent")

                // Создаем обычного пользователя
                if (existingUser == null) {
                    val testUser = User(
                        id = null,
                        login = "testuser",
                        password = "Test123",
                        email = "user@test.com",
                        isAgent = false,
                        totalPurchases = 0,
                        discountLevel = 0,
                        totalSpent = 0.0
                    )
                    db.getUserDao().insertItem(testUser)
                }

                // Создаем агента
                if (existingAgent == null) {
                    val testAgent = User(
                        id = null,
                        login = "testagent",
                        password = "Agent123",
                        email = "agent@test.com",
                        isAgent = true,
                        totalPurchases = 0,
                        discountLevel = 0,
                        totalSpent = 0.0
                    )
                    db.getUserDao().insertItem(testAgent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}