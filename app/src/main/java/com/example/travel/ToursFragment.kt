package com.example.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject

class ToursFragment(val user: User) : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonBack: Button
    private var currentTours = mutableListOf<CurrentTour>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tours, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        buttonBack = view.findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            val mainClientFragment = MainClientFragment(user)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mainClientFragment)
                .commit()
        }

        val db = MainDB.getDb(requireContext())
        db.getTourDao().getAllItems().asLiveData()
            .observe(viewLifecycleOwner) { tours ->
                currentTours.clear()
                currentTours.addAll(tours)

                viewPager.adapter = CustomPagerAdapter(
                    currentTours,
                    requireContext(),
                    user,
                    parentFragmentManager
                )

                viewPager.setPageTransformer { page, position ->
                    val scale = 0.85f + (1 - Math.abs(position)) * 0.15f
                    page.scaleX = scale
                    page.scaleY = scale
                }

                for (i in currentTours.indices) {
                    requestFlagAndPicture(currentTours[i].country, i)
                }
            }

        return view
    }

    private fun requestFlagAndPicture(country: String, index: Int) {
        val url = "https://restcountries.com/v3.1/name/$country"
        val queue = Volley.newRequestQueue(requireContext())

        val request = StringRequest(
            Request.Method.GET,
            url,
            { result -> parseTourData(result, index) },
            { error ->
                Log.e("ToursFragment", "Error: ${error.message}")
                Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    private fun parseTourData(result: String, index: Int) {
        try {
            val mainObject = JSONArray(result)
            val countryObject: JSONObject = mainObject.getJSONObject(0)
            currentTours[index].flagUrl = countryObject.getJSONObject("flags").getString("png")

            val capitals = countryObject.getJSONArray("capital")
            if (capitals.length() > 0) {
                requestCapitalPicture(capitals.getString(0), index)
            }
        } catch (e: Exception) {
            Log.e("ToursFragment", "Error parsing tour data", e)
        }
    }

    private fun requestCapitalPicture(capital: String, index: Int) {
        val key = "4o1ii7y8ZafX76F1gVBQD_YoTozmEqH1Am3XCNDUJ4c"
        val url = "https://api.unsplash.com/search/photos?query=$capital&count=1&orientation=portrait&client_id=$key"
        val queue = Volley.newRequestQueue(requireContext())

        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                try {
                    val mainObject = JSONObject(result)
                    val results = mainObject.getJSONArray("results")
                    if (results.length() > 0) {
                        currentTours[index].imageUrl = results
                            .getJSONObject(0)
                            .getJSONObject("urls")
                            .getString("regular")
                    }
                } catch (e: Exception) {
                    Log.e("ToursFragment", "Error parsing image data", e)
                }
            },
            { error ->
                Log.e("ToursFragment", "Error loading image: ${error.message}")
            }
        )
        queue.add(request)
    }

    // Вложенный класс CustomPagerAdapter
    inner class CustomPagerAdapter(
        private val items: MutableList<CurrentTour>,
        private val context: Context,
        private val user: User,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder>() {

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            val titleView: TextView = itemView.findViewById(R.id.titleText)
            val priceView: TextView = itemView.findViewById(R.id.priceText)
            val userName: EditText = itemView.findViewById(R.id.buyerName)
            val countryView: TextView = itemView.findViewById(R.id.countryText)
            val dateView: TextView = itemView.findViewById(R.id.dateText)
            val buyButton: Button = itemView.findViewById(R.id.buyButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tour, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = items[position]

            if (item.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.top_bar)
                    .fit()
                    .into(holder.imageView)
            }

            holder.dateView.text = item.date
            holder.titleView.text = item.name
            holder.countryView.text = item.country

            val discountPercent = when (user.discountLevel) {
                1 -> 5
                2 -> 10
                3 -> 15
                else -> 0
            }

            val priceWithDiscount = item.price - (item.price * discountPercent / 100)

            if (discountPercent > 0) {
                holder.priceView.text = "${item.price}₽ → ${priceWithDiscount}₽ (скидка ${discountPercent}%)"
            } else {
                holder.priceView.text = "${item.price}₽"
            }

            holder.buyButton.setOnClickListener {
                if (holder.userName.text.toString().isNotEmpty()) {
                    val db = MainDB.getDb(context)
                    Thread {
                        if (!db.getBoughtTourDao().tourBought(
                                item.name,
                                user.login,
                                holder.userName.text.toString()
                            )
                        ) {
                            val finalPrice = item.price - (item.price * discountPercent / 100)

                            val boughtTour = BoughtTour(
                                userLogin = user.login,
                                name = item.name,
                                username = holder.userName.text.toString(),
                                country = item.country,
                                date = item.date,
                                flagUrl = item.flagUrl,
                                imageUrl = item.imageUrl,
                                originalPrice = item.price,
                                discountApplied = discountPercent,
                                finalPrice = finalPrice
                            )

                            db.getBoughtTourDao().insertItem(boughtTour)

                            val currentUser = db.getUserDao().getUserByLogin(user.login)
                            currentUser?.let { user ->
                                user.totalPurchases += 1
                                user.totalSpent += finalPrice.toDouble()

                                user.discountLevel = when {
                                    user.totalSpent > 300000 -> 3
                                    user.totalSpent > 150000 -> 2
                                    user.totalPurchases > 5 -> 1
                                    else -> 0
                                }

                                db.getUserDao().updateUser(user)
                            }

                            (context as? android.app.Activity)?.runOnUiThread {
                                Snackbar.make(
                                    holder.buyButton,
                                    "Тур '${item.name}' куплен за ${finalPrice}₽!",
                                    Snackbar.LENGTH_LONG
                                ).show()

                                val mainClientFragment = MainClientFragment(user)
                                fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, mainClientFragment)
                                    .commit()
                            }
                        } else {
                            (context as? android.app.Activity)?.runOnUiThread {
                                Snackbar.make(
                                    holder.buyButton,
                                    "Вы уже покупали этот тур",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.start()
                } else {
                    Snackbar.make(
                        holder.userName,
                        "Введите имя покупателя",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }
}