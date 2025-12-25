package com.example.travel

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button
    private lateinit var loginText: EditText
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var confirmPasswordText: EditText
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var clientRadio: RadioButton
    private lateinit var agentRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnRegister = findViewById(R.id.btnRegister)
        btnBack = findViewById(R.id.btnBack)
        loginText = findViewById(R.id.registerLogin)
        emailText = findViewById(R.id.registerEmail)
        passwordText = findViewById(R.id.registerPassword)
        confirmPasswordText = findViewById(R.id.registerConfirmPassword)
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        clientRadio = findViewById(R.id.clientRadio)
        agentRadio = findViewById(R.id.agentRadio)

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegister.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }
    }

    private fun validateInput(): Boolean {
        val login = loginText.text.toString().trim()
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        val confirmPassword = confirmPasswordText.text.toString().trim()

        // Проверка логина
        if (login.isEmpty()) {
            showError("Введите логин")
            return false
        }
        if (login.length < 3) {
            showError("Логин должен содержать минимум 3 символа")
            return false
        }
        if (!login.matches(Regex("^[a-zA-Z0-9]+\$"))) {
            showError("Логин должен содержать только буквы и цифры")
            return false
        }

        // Проверка email
        if (email.isEmpty()) {
            showError("Введите email")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email")
            return false
        }

        // Проверка пароля
        if (password.isEmpty()) {
            showError("Введите пароль")
            return false
        }
        if (password.length < 6) {
            showError("Пароль должен содержать минимум 6 символов")
            return false
        }
        if (!password.matches(Regex(".*[A-Z].*"))) {
            showError("Пароль должен содержать хотя бы одну заглавную букву")
            return false
        }
        if (!password.matches(Regex(".*[0-9].*"))) {
            showError("Пароль должен содержать хотя бы одну цифру")
            return false
        }

        // Проверка подтверждения пароля
        if (confirmPassword.isEmpty()) {
            showError("Подтвердите пароль")
            return false
        }
        if (password != confirmPassword) {
            showError("Пароли не совпадают")
            return false
        }

        return true
    }

    private fun registerUser() {
        val login = loginText.text.toString().trim()
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        val isAgent = agentRadio.isChecked

        // Показываем индикатор загрузки
        btnRegister.isEnabled = false
        btnRegister.text = "Регистрация..."

        Thread {
            try {
                val db = MainDB.getDb(this@RegisterActivity)
                val userDao = db.getUserDao()

                // Проверяем, существует ли пользователь с таким логином
                val existingUser = userDao.getUserByLogin(login)
                if (existingUser != null) {
                    runOnUiThread {
                        btnRegister.isEnabled = true
                        btnRegister.text = "Зарегистрироваться"
                        showError("Пользователь с таким логином уже существует")
                    }
                    return@Thread
                }

                // Проверяем, существует ли пользователь с таким email
                val existingEmail = userDao.getUserByEmail(email)
                if (existingEmail != null) {
                    runOnUiThread {
                        btnRegister.isEnabled = true
                        btnRegister.text = "Зарегистрироваться"
                        showError("Пользователь с таким email уже существует")
                    }
                    return@Thread
                }

                // Создаем нового пользователя
                val newUser = User(
                    id = null,
                    login = login,
                    password = password,
                    email = email,
                    isAgent = isAgent,
                    totalPurchases = 0,
                    discountLevel = 0,
                    totalSpent = 0.0
                )

                // Сохраняем в базу данных
                userDao.insertItem(newUser)

                // Показываем сообщение об успехе
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна! Теперь войдите в систему",
                        Toast.LENGTH_LONG
                    ).show()

                    // Возвращаемся на главную страницу
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Зарегистрироваться"
                    showError("Ошибка при регистрации: ${e.message}")
                }
            }
        }.start()
    }

    private fun showError(message: String) {
        Snackbar.make(btnRegister, message, Snackbar.LENGTH_LONG).show()
    }
}