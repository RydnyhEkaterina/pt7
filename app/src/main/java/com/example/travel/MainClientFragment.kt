package com.example.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainClientFragment(val user: User) : Fragment() {

    private var boughtTourList = mutableListOf<BoughtTour>()
    private var filteredTourList = mutableListOf<BoughtTour>()
    private lateinit var buttonOpenTours: Button
    private lateinit var buttonCountries: Button
    private lateinit var boughtRecycler: RecyclerView
    private lateinit var welcomeText: TextView
    private lateinit var searchView: SearchView
    private lateinit var totalPurchasesText: TextView
    private lateinit var discountLevelText: TextView
    private lateinit var totalSpentText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_client, container, false)

        buttonOpenTours = view.findViewById(R.id.buttonOpenTours)
        buttonCountries = view.findViewById(R.id.buttonCountries)
        welcomeText = view.findViewById(R.id.welcomeText)
        boughtRecycler = view.findViewById(R.id.boughtRecycler)
        searchView = view.findViewById(R.id.searchView)
        totalPurchasesText = view.findViewById(R.id.totalPurchasesText)
        discountLevelText = view.findViewById(R.id.discountLevelText)
        totalSpentText = view.findViewById(R.id.totalSpentText)

        welcomeText.text = "Клиент: ${user.login}"
        updateClientInfo()
        setupSearch()
        loadPurchasedTours()

        buttonOpenTours.setOnClickListener {
            navigateToTours()
        }

        buttonCountries.setOnClickListener {
            navigateToCountries()
        }

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterTours(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterTours(newText)
                return true
            }
        })
    }

    private fun filterTours(query: String) {
        if (query.isEmpty()) {
            filteredTourList.clear()
            filteredTourList.addAll(boughtTourList)
        } else {
            filteredTourList.clear()
            boughtTourList.forEach {
                if (it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true)
                ) {
                    filteredTourList.add(it)
                }
            }
        }
        updateRecyclerView()
    }

    private fun loadPurchasedTours() {
        val db = MainDB.getDb(requireContext())
        db.getBoughtTourDao().getAllUserItems(user.login).asLiveData()
            .observe(requireContext() as LifecycleOwner) { tours ->
                boughtTourList.clear()
                boughtTourList.addAll(tours)
                filteredTourList.clear()
                filteredTourList.addAll(tours)
                updateRecyclerView()
            }
    }

    private fun updateRecyclerView() {
        boughtRecycler.layoutManager = LinearLayoutManager(requireContext())
        boughtRecycler.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user_tour, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            // MainClientFragment.kt - исправленный метод onBindViewHolder

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tour = filteredTourList[position]
                val titleText = holder.itemView.findViewById<TextView>(R.id.itemTitle)
                val countryText = holder.itemView.findViewById<TextView>(R.id.itemCountry)
                val dateText = holder.itemView.findViewById<TextView>(R.id.itemDate)
                val usernameText = holder.itemView.findViewById<TextView>(R.id.itemUsername)
                val priceText = holder.itemView.findViewById<TextView>(R.id.itemPrice)

                titleText.text = tour.name
                countryText.text = "Страна: ${tour.country}"
                dateText.text = "Дата: ${tour.date}"
                usernameText.text = "Покупатель: ${tour.username}"

                // ПРАВИЛЬНЫЙ СПОСОБ - используем поля класса BoughtTour
                if (tour.discountApplied > 0) {
                    priceText.text = "Цена: ${tour.finalPrice}₽ (скидка ${tour.discountApplied}%)"
                } else {
                    priceText.text = "Цена: ${tour.finalPrice}₽"
                }
            }

            override fun getItemCount() = filteredTourList.size
        }
    }

    private fun updateClientInfo() {
        val db = MainDB.getDb(requireContext())
        Thread {
            // Используем данные из User, пока нет OrderDao
            val orderCount = user.totalPurchases
            val totalSpent = user.totalSpent

            val newDiscountLevel = calculateDiscountLevel(orderCount, totalSpent)
            if (user.discountLevel != newDiscountLevel) {
                user.discountLevel = newDiscountLevel
                db.getUserDao().updateDiscountLevel(user.login, newDiscountLevel)
            }

            requireActivity().runOnUiThread {
                totalPurchasesText.text = "Куплено туров: $orderCount"
                totalSpentText.text = "Потрачено: ${totalSpent.toInt()}₽"

                val discountPercent = when (newDiscountLevel) {
                    1 -> 5
                    2 -> 10
                    3 -> 15
                    else -> 0
                }
                discountLevelText.text = "Уровень скидки: $discountPercent%"
            }
        }.start()
    }

    private fun calculateDiscountLevel(orderCount: Int, totalSpent: Double): Int {
        return when {
            totalSpent > 300000 -> 3      // 15% скидка
            totalSpent > 150000 -> 2      // 10% скидка
            orderCount > 5 -> 1           // 5% скидка
            else -> 0                     // Без скидки
        }
    }

    private fun navigateToTours() {
        val toursFragment = ToursFragment(user)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, toursFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCountries() {
        val countriesFragment = CountriesFragment(user)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, countriesFragment)
            .addToBackStack(null)
            .commit()
    }
}