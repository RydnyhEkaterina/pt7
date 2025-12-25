package com.example.travel

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLogin: Button
    private lateinit var btnBack: Button
    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var clientRadio: RadioButton
    private lateinit var agentRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.btnLogin)
        btnBack = findViewById(R.id.btnBack)
        loginText = findViewById(R.id.loginText)
        passwordText = findViewById(R.id.loginPassword)
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        clientRadio = findViewById(R.id.clientRadio)
        agentRadio = findViewById(R.id.agentRadio)

        sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE)

        loadSavedData()

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            if (validateInput()) {
                authenticateUser()
            }
        }
    }

    private fun validateInput(): Boolean {
        val login = loginText.text.toString().trim()
        val password = passwordText.text.toString().trim()

        if (login.isEmpty()) {
            showError("Введите логин")
            return false
        }

        if (password.isEmpty()) {
            showError("Введите пароль")
            return false
        }

        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            return false
        }

        return true
    }

    private fun authenticateUser() {
        val login = loginText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        val isAgent = agentRadio.isChecked

        val db = MainDB.getDb(this)

        Thread {
            try {
                val user = db.getUserDao().getUserByLogin(login)

                runOnUiThread {
                    if (user != null) {
                        if (user.password == password) {
                            if (user.isAgent == isAgent) {
                                // Сохраняем данные пользователя
                                saveUserData(user, isAgent)

                                // Переход к соответствующему фрагменту
                                navigateToUserFragment(user, isAgent)
                            } else {
                                showError("Неправильный режим работы для данного пользователя")
                            }
                        } else {
                            showError("Неверный пароль")
                        }
                    } else {
                        showError("Пользователь с таким логином не найден")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Ошибка базы данных: ${e.message}")
                }
            }
        }.start()
    }

    private fun saveUserData(user: User, isAgent: Boolean) {
        sharedPreferences.edit().apply {
            putString("CURRENT_USER_LOGIN", user.login)
            putString("CURRENT_USER_PASSWORD", user.password)
            putBoolean("CURRENT_USER_IS_AGENT", isAgent)
            putString("CURRENT_USER_EMAIL", user.email)
            apply()
        }
    }

    private fun loadSavedData() {
        val savedLogin = sharedPreferences.getString("CURRENT_USER_LOGIN", "")
        val savedPassword = sharedPreferences.getString("CURRENT_USER_PASSWORD", "")
        val savedIsAgent = sharedPreferences.getBoolean("CURRENT_USER_IS_AGENT", false)

        if (!savedLogin.isNullOrEmpty()) {
            loginText.setText(savedLogin)
            passwordText.setText(savedPassword)

            if (savedIsAgent) {
                agentRadio.isChecked = true
            } else {
                clientRadio.isChecked = true
            }
        }
    }

    private fun navigateToUserFragment(user: User, isAgent: Boolean) {
        val intent = Intent(this, UserHomeActivity::class.java).apply {
            putExtra("USER", user)
            putExtra("USER_AGENT", isAgent)
        }
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Snackbar.make(btnLogin, message, Snackbar.LENGTH_LONG).show()
    }
}