package com.example.travel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class CustomPagerAdapter(
    private val items: MutableList<CurrentTour>,
    private val context: Context,
    val user: User,
    val fragmentManager: FragmentManager
) : RecyclerView.Adapter<CustomPagerAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        // Загрузка изображений
        if (item.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(item.imageUrl)
                .placeholder(R.drawable.top_bar)
                .fit()
                .into(holder.imageView)
        }

        if (item.flagUrl.isNotEmpty()) {
            Picasso.get()
                .load(item.flagUrl)
                .placeholder(R.drawable.top_bar)
                .fit()
        }

        // Отображение информации о туре
        holder.dateView.text = item.date
        holder.titleView.text = item.name
        holder.countryView.text = item.country

        // Отображение цены со скидкой пользователя
        val discountPercent = when (user.discountLevel) {
            1 -> 5
            2 -> 10
            3 -> 15
            else -> 0
        }

        val priceWithDiscount = item.price - (item.price * discountPercent / 100)

        if (discountPercent > 0) {
            holder.priceView.text = "${item.price}₽ → ${priceWithDiscount}₽ (скидка ${discountPercent}%)"
            holder.priceView.setTextColor(context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.priceView.text = "${item.price}₽"
            holder.priceView.setTextColor(context.getColor(android.R.color.white))
        }

        // Обработка покупки
        holder.buyButton.setOnClickListener {
            if (holder.userName.text.toString().isNotEmpty()) {
                val db = MainDB.getDb(context)
                Thread {
                    // Проверяем, не куплен ли уже этот тур
                    if (!db.getBoughtTourDao().tourBought(
                            item.name,
                            user.login,
                            holder.userName.text.toString()
                        )
                    ) {
                        // Рассчитываем скидку
                        val finalPrice = item.price - (item.price * discountPercent / 100)

                        // ПРОВЕРЬТЕ: убедитесь, что у BoughtTour есть конструктор с этими параметрами
                        // Если нет, обновите BoughtTour.kt как показано ниже
                        val boughtTour = try {
                            // Попытка создать с дополнительными полями цены
                            BoughtTour(
                                id = null,
                                userLogin = user.login,
                                name = item.name,
                                username = holder.userName.text.toString(),
                                country = item.country,
                                date = item.date,
                                flagUrl = item.flagUrl,
                                imageUrl = item.imageUrl,
                                originalPrice = item.price,      // ДОБАВЬТЕ ЭТО ПОЛЕ В BoughtTour
                                discountApplied = discountPercent, // ДОБАВЬТЕ ЭТО ПОЛЕ В BoughtTour
                                finalPrice = finalPrice          // ДОБАВЬТЕ ЭТО ПОЛЕ В BoughtTour
                            )
                        } catch (e: Exception) {
                            // Если конструктора нет, создаем простую версию
                            // НО вам нужно ОБНОВИТЬ BoughtTour.kt!
                            BoughtTour(
                                id = null,
                                userLogin = user.login,
                                name = item.name,
                                username = holder.userName.text.toString(),
                                country = item.country,
                                date = item.date,
                                flagUrl = item.flagUrl,
                                imageUrl = item.imageUrl
                                // Цены не сохранятся!
                            )
                        }

                        db.getBoughtTourDao().insertItem(boughtTour)

                        // Обновляем статистику пользователя
                        // Обновляем статистику пользователя
                        try {
                            // Используем правильные параметры
                            db.getUserDao().updatePurchaseStats(
                                login = user.login,
                                purchases = 1, // увеличиваем на 1 покупку
                                spent = finalPrice.toDouble(),
                                discount = discountPercent
                            )
                        } catch (e: NoSuchMethodError) {
                            // Если метода нет, обновляем пользователя вручную
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
                        }

                        // Переход на главную
                        (context as? android.app.Activity)?.runOnUiThread {
                            Snackbar.make(
                                holder.buyButton,
                                "Тур '${item.name}' куплен за ${finalPrice}₽!",
                                Snackbar.LENGTH_LONG
                            ).show()

                            val mainClientFragment = MainClientFragment(user)
                            fragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_container, mainClientFragment)
                                .commit()
                        }
                    } else {
                        // Тур уже куплен
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