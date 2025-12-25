// CountryToursFragment.kt
package com.example.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class CountryToursFragment(val user: User, val countryName: String) : Fragment() {

    private lateinit var toursRecycler: RecyclerView
    private lateinit var backButton: Button
    private lateinit var countryTitle: TextView
    private var toursList = mutableListOf<CurrentTour>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_country_tours, container, false)

        toursRecycler = view.findViewById(R.id.toursRecycler)
        backButton = view.findViewById(R.id.backButton)
        countryTitle = view.findViewById(R.id.countryTitle)

        countryTitle.text = "Туры в $countryName"
        setupRecyclerView()
        loadCountryTours()

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun setupRecyclerView() {
        toursRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadCountryTours() {
        val db = MainDB.getDb(requireContext())

        db.getTourDao().getAllItems().asLiveData()
            .observe(viewLifecycleOwner) { tours ->
                toursList.clear()
                tours.forEach { tour ->
                    if (tour.country.equals(countryName, ignoreCase = true)) {
                        toursList.add(tour)
                    }
                }

                if (toursList.isEmpty()) {
                    showNoToursMessage()
                } else {
                    updateToursAdapter()
                }
            }
    }

    private fun showNoToursMessage() {
        // Показываем сообщение, если туров нет
        val noToursView = TextView(requireContext())
        noToursView.text = "😔\nВ $countryName пока нет доступных туров\n\nПопробуйте позже"
        noToursView.textSize = 18f
        noToursView.gravity = android.view.Gravity.CENTER
        toursRecycler.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(noToursView) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun getItemCount() = 1
        }
    }

    private fun updateToursAdapter() {
        toursRecycler.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tour_simple, parent, false)
                return TourViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tour = toursList[position]
                val tourHolder = holder as TourViewHolder

                tourHolder.titleText.text = tour.name
                tourHolder.dateText.text = "Дата: ${tour.date}"
                tourHolder.priceText.text = "Цена: ${tour.price}₽"
                tourHolder.seatsText.text = "Мест: ${tour.availableSeats}"
                tourHolder.durationText.text = "Дней: ${tour.durationDays}"

                // Кнопка покупки
                holder.itemView.setOnClickListener {
                    buyTour(tour)
                }
            }

            override fun getItemCount() = toursList.size
        }
    }

    // ViewHolder для тура
    inner class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tourTitle)
        val dateText: TextView = itemView.findViewById(R.id.tourDate)
        val priceText: TextView = itemView.findViewById(R.id.tourPrice)
        val seatsText: TextView = itemView.findViewById(R.id.tourSeats)
        val durationText: TextView = itemView.findViewById(R.id.tourDuration)
    }

    // МЕТОД ДЛЯ ПОКУПКИ ТУРА
    private fun buyTour(tour: CurrentTour) {
        try {
            val db = MainDB.getDb(requireContext())

            // Проверяем, есть ли уже этот тур у пользователя
            Thread {
                val alreadyBought = db.getBoughtTourDao().tourBought(
                    tour.name,
                    user.login,
                    user.login
                )

                requireActivity().runOnUiThread {
                    if (alreadyBought) {
                        Toast.makeText(
                            requireContext(),
                            "Вы уже купили этот тур!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runOnUiThread
                    }

                    // Вместо диалога подтверждения сразу открываем фрагмент оформления
                    openTourPurchaseFragment(tour)
                }
            }.start()

        } catch (e: Exception) {
            Log.e("DEBUG", "Error buying tour: ${e.message}")
            Toast.makeText(requireContext(), "Ошибка покупки", Toast.LENGTH_SHORT).show()
        }
    }

    // Новый метод для открытия фрагмента оформления покупки
    private fun openTourPurchaseFragment(tour: CurrentTour) {
        val purchaseFragment = TourPurchaseFragment(user, tour)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, purchaseFragment)
            .addToBackStack("purchase")
            .commit()
    }

    private fun showBuyConfirmationDialog(tour: CurrentTour, db: MainDB) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение покупки")
            .setMessage(
                "Вы хотите купить тур:\n" +
                        "\"${tour.name}\"\n" +
                        "Дата: ${tour.date}\n" +
                        "Цена: ${tour.price}₽\n" +
                        "Дней: ${tour.durationDays}"
            )
            .setPositiveButton("Купить") { dialog, which ->
                processTourPurchase(tour, db)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun processTourPurchase(tour: CurrentTour, db: MainDB) {
        // Рассчитываем скидку (простая логика)
        val discount = calculateDiscount(user)
        val finalPrice = tour.price * (100 - discount) / 100

        // Создаем запись о покупке
        val boughtTour = BoughtTour(
            userLogin = user.login,
            name = tour.name,
            username = user.login,
            country = tour.country,
            date = tour.date,
            flagUrl = tour.flagUrl,
            imageUrl = tour.imageUrl,
            originalPrice = tour.price,
            discountApplied = discount,
            finalPrice = finalPrice
        )

        Thread {
            // Добавляем покупку
            db.getBoughtTourDao().insertItem(boughtTour)

            // Уменьшаем количество мест
            tour.availableSeats -= 1
            db.getTourDao().updateTour(tour)

            // Обновляем статистику пользователя
            updateUserStats()

            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "✅ Тур успешно куплен!\n" +
                            "Скидка: $discount%\n" +
                            "Итоговая цена: $finalPrice₽",
                    Toast.LENGTH_LONG
                ).show()

                // Обновляем список туров (места изменились)
                loadCountryTours()
            }
        }.start()
    }

    private fun calculateDiscount(user: User): Int {
        // Простая логика скидок
        val db = MainDB.getDb(requireContext())

        // Временное решение - просто возвращаем фиксированную скидку
        // Позже можно улучшить логику
        return 0
    }

    private fun updateUserStats() {
        // Можно обновить статистику в MainClientFragment
        // или просто показать уведомление
    }
}