package com.example.travel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class EditUserFragment(var user: User, val useradmin: User) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_user, container, false)

        var loginText = view.findViewById<EditText>(R.id.editUserLogin)
        var passwordText = view.findViewById<EditText>(R.id.editUserPassword)
        var editButton = view.findViewById<Button>(R.id.saveUser)

        loginText.setText(user.login)
        passwordText.setText(user.password)

        editButton.setOnClickListener {
            // Создаем новый объект User с обновленными данными
            val updatedUser = User(
                id = user.id, // сохраняем тот же ID
                login = loginText.text.toString(),
                password = passwordText.text.toString(),
                email = user.email, // сохраняем email
                isAgent = user.isAgent, // сохраняем статус агента
                totalPurchases = user.totalPurchases, // сохраняем статистику
                discountLevel = user.discountLevel,
                totalSpent = user.totalSpent
            )

            val db = MainDB.getDb(requireContext())
            Thread {
                db.getUserDao().updateUser(updatedUser)

                requireActivity().runOnUiThread {
                    val mainAdminFragment = AdminFragment(useradmin)
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, mainAdminFragment)
                        .commit()
                }
            }.start()
        }

        return view
    }
}