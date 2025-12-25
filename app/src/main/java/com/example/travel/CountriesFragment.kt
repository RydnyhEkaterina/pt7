package com.example.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class CountriesFragment(val user: User) : Fragment() {

    private lateinit var countriesRecycler: RecyclerView
    private lateinit var backButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var searchView: android.widget.SearchView

    private var countriesList = mutableListOf<Country>()
    private var filteredList = mutableListOf<Country>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_countries, container, false)

        countriesRecycler = view.findViewById(R.id.countriesRecycler)
        backButton = view.findViewById(R.id.backButton)
        progressBar = view.findViewById(R.id.progressBar)
        errorText = view.findViewById(R.id.errorText)
        retryButton = view.findViewById(R.id.retryButton)
        searchView = view.findViewById(R.id.searchView)

        setupRecyclerView()
        setupSearch()
        loadCountries()

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        retryButton.setOnClickListener {
            loadCountries()
        }

        return view
    }

    private fun setupRecyclerView() {
        countriesRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        countriesRecycler.setHasFixedSize(true)
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterCountries(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterCountries(newText)
                return true
            }
        })
    }

    private fun filterCountries(query: String) {
        if (query.isEmpty()) {
            filteredList.clear()
            filteredList.addAll(countriesList)
        } else {
            filteredList.clear()
            countriesList.forEach { country ->
                if (country.name.contains(query, ignoreCase = true) ||
                    country.capital.contains(query, ignoreCase = true)) {
                    filteredList.add(country)
                }
            }
        }
        updateAdapter()
    }

    private fun loadCountries() {
        showLoading(true)
        showError(false)

        lifecycleScope.launch {
            try {
                // Имитация запроса к API
                countriesList = MockApiService.fetchCountries()

                if (countriesList.isEmpty()) {
                    showError(true, "Нет доступных стран")
                } else {
                    filteredList.clear()
                    filteredList.addAll(countriesList)

                    requireActivity().runOnUiThread {
                        updateAdapter()
                        Toast.makeText(
                            requireContext(),
                            "Загружено ${countriesList.size} стран",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showError(true, "Ошибка загрузки: ${e.message}")
                }
            } finally {
                requireActivity().runOnUiThread {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        countriesRecycler.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(show: Boolean, message: String = "") {
        errorText.visibility = if (show) View.VISIBLE else View.GONE
        retryButton.visibility = if (show) View.VISIBLE else View.GONE
        if (show && message.isNotEmpty()) {
            errorText.text = message
        }
    }

    private fun updateAdapter() {
        countriesRecycler.adapter = object : RecyclerView.Adapter<CountryViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_country, parent, false)
                return CountryViewHolder(view)
            }

            override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
                val country = filteredList[position]
                holder.bind(country)

                holder.itemView.setOnClickListener {
                    navigateToCountryTours(country.name)
                }
            }

            override fun getItemCount() = filteredList.size
        }
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val countryName: TextView = itemView.findViewById(R.id.countryName)
        private val capitalText: TextView = itemView.findViewById(R.id.capitalText)
        private val toursCountText: TextView = itemView.findViewById(R.id.toursCountText)
        private val popularityText: TextView = itemView.findViewById(R.id.popularityText)
        private val flagImage: ImageView = itemView.findViewById(R.id.flagImage)
        private val loadingIndicator: View = itemView.findViewById(R.id.imageLoading)

        fun bind(country: Country) {
            countryName.text = country.name
            capitalText.text = "Столица: ${country.capital}"

            // Имитация подсчета туров (случайное число)
            val tourCount = (1..8).random()
            toursCountText.text = "Доступно туров: $tourCount"

            // Показываем рейтинг популярности
            popularityText.text = "★".repeat(country.popularity)
            popularityText.setTextColor(when (country.popularity) {
                5 -> 0xFF4CAF50.toInt()  // Зеленый
                4 -> 0xFF2196F3.toInt()  // Синий
                3 -> 0xFFFF9800.toInt()  // Оранжевый
                else -> 0xFF757575.toInt() // Серый
            })

            // Показываем индикатор загрузки
            loadingIndicator.visibility = View.VISIBLE

            // Загружаем флаг с эффектом загрузки
            Picasso.get()
                .load(country.flagUrl)
                .placeholder(android.R.drawable.ic_menu_compass)
                .error(android.R.drawable.stat_notify_error)
                .into(flagImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        loadingIndicator.visibility = View.GONE
                    }
                    override fun onError(e: Exception?) {
                        loadingIndicator.visibility = View.GONE
                        flagImage.setImageResource(android.R.drawable.ic_menu_map)
                    }
                })
        }
    }

    private fun navigateToCountryTours(countryName: String) {
        Toast.makeText(requireContext(), "Загрузка туров для $countryName...", Toast.LENGTH_SHORT).show()

        val countryToursFragment = CountryToursFragment(user, countryName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, countryToursFragment)
            .addToBackStack("countries")
            .commit()
    }
}