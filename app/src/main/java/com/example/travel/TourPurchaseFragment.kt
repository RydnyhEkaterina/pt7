package com.example.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TourPurchaseFragment(val user: User, val tour: CurrentTour) : Fragment() {

    private lateinit var tourTitle: TextView
    private lateinit var tourDate: TextView
    private lateinit var tourPrice: TextView
    private lateinit var tourDuration: TextView
    private lateinit var tourSeats: TextView
    private lateinit var tourCountry: TextView

    private lateinit var lastNameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tour_purchase, container, false)

        // Инициализация всех view
        tourTitle = view.findViewById(R.id.tourTitle)
        tourDate = view.findViewById(R.id.tourDate)
        tourPrice = view.findViewById(R.id.tourPrice)
        tourDuration = view.findViewById(R.id.tourDuration)
        tourSeats = view.findViewById(R.id.tourSeats)
        tourCountry = view.findViewById(R.id.tourCountry)

        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        middleNameEditText = view.findViewById(R.id.middleNameEditText)
        confirmButton = view.findViewById(R.id.confirmButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Заполняем информацию о туре (с проверками)
        tourTitle.text = tour.name
        tourDate.text = "Дата: ${tour.date}"
        tourPrice.text = "Цена: ${tour.price}₽"

        // Проверяем, инициализированы ли поля
        val duration = if (tour.durationDays > 0) tour.durationDays else 7
        tourDuration.text = "Длительность: $duration дней"

        val seats = if (tour.availableSeats > 0) tour.availableSeats else 0
        tourSeats.text = "Осталось мест: $seats"
        tourCountry.text = "Страна: ${tour.country}"

        // Кнопка отмены
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Кнопка подтверждения покупки
        confirmButton.setOnClickListener {
            processPurchase()
        }

        return view
    }

    private fun processPurchase() {
        val lastName = lastNameEditText.text.toString().trim()
        val firstName = firstNameEditText.text.toString().trim()
        val middleName = middleNameEditText.text.toString().trim()

        // Проверка заполненности полей
        if (lastName.isEmpty() || firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните фамилию и имя", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка доступности мест
        if (tour.availableSeats <= 0) {
            Toast.makeText(
                requireContext(),
                "Извините, места закончились",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Формируем полное имя
        val fullName = if (middleName.isNotEmpty()) {
            "$lastName $firstName $middleName"
        } else {
            "$lastName $firstName"
        }

        // Используем корутины для асинхронной работы
        lifecycleScope.launch {
            try {
                purchaseTour(fullName)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка при покупке: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private suspend fun purchaseTour(fullName: String) {
        // Рассчитываем скидку
        val discount = calculateDiscount(user)
        val finalPrice = tour.price * (100 - discount) / 100

        // Создаем запись о покупке
        val boughtTour = BoughtTour(
            userLogin = user.login,
            name = tour.name,
            username = fullName,
            country = tour.country,
            date = tour.date,
            flagUrl = tour.flagUrl,
            imageUrl = tour.imageUrl,
            originalPrice = tour.price,
            discountApplied = discount,
            finalPrice = finalPrice
        )

        // Выполняем в фоновом потоке
        withContext(Dispatchers.IO) {
            val db = MainDB.getDb(requireContext())

            // Добавляем покупку
            db.getBoughtTourDao().insertItem(boughtTour)

            // Обновляем тур (уменьшаем количество мест)
            tour.availableSeats -= 1
            db.getTourDao().updateTour(tour)

            // Обновляем статистику пользователя
            updateUserStats(db, finalPrice)
        }

        // Возвращаемся в основной поток для обновления UI
        withContext(Dispatchers.Main) {
            showSuccessMessage(fullName, discount, finalPrice)
            navigateToMainClient()
        }
    }

    private fun calculateDiscount(user: User): Int {
        return when {
            user.totalPurchases >= 5 -> 15
            user.totalPurchases >= 3 -> 10
            user.totalPurchases >= 1 -> 5
            else -> 0
        }
    }

    private fun updateUserStats(db: MainDB, finalPrice: Int) {
        try {
            val currentUser = db.getUserDao().getUserByLogin(user.login)
            currentUser?.let { user ->
                user.totalPurchases += 1
                user.totalSpent += finalPrice.toDouble()

                // Обновляем уровень скидки
                user.discountLevel = when {
                    user.totalSpent > 300000 -> 3
                    user.totalSpent > 150000 -> 2
                    user.totalPurchases > 5 -> 1
                    else -> 0
                }

                db.getUserDao().updateUser(user)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSuccessMessage(fullName: String, discount: Int, finalPrice: Int) {
        Toast.makeText(
            requireContext(),
            "✅ Тур успешно куплен!\n" +
                    "Заказ оформлен на: $fullName\n" +
                    "Скидка: $discount%\n" +
                    "Итоговая цена: $finalPrice₽",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToMainClient() {
        try {
            val mainClientFragment = MainClientFragment(user)

            // Используем requireActivity() для получения FragmentManager
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mainClientFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}