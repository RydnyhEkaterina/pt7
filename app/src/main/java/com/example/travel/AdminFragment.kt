package com.example.travel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminFragment(val user: User) : Fragment() {

    var usersList = mutableListOf<User>()
    var currentToursList = mutableListOf<CurrentTour>()

    private lateinit var datePicker : DatePicker

    private lateinit var welcomeText : TextView
    private lateinit var addTitleText : EditText
    private lateinit var addTourPrice : EditText
    private lateinit var countrySpinner : Spinner
    private lateinit var addTourButton : Button
    private lateinit var recyclerTours : RecyclerView
    private lateinit var recyclerUsers : RecyclerView

    val countries = arrayOf(
        "russia",
        "usa",
        "germany",
        "france",
        "japan",
        "china"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_admin, container, false)
        welcomeText = view.findViewById(R.id.welcomeText)
        welcomeText.setText(user.login)
        datePicker = view.findViewById(R.id.addTourDatePicker)
        addTitleText = view.findViewById(R.id.addTourTitle)
        addTourPrice = view.findViewById(R.id.addTourPrice)
        countrySpinner = view.findViewById(R.id.addTourSpinner)
        addTourButton = view.findViewById(R.id.addTourButton)

        recyclerTours = view.findViewById(R.id.recyclerAdminTours)
        recyclerUsers = view.findViewById(R.id.recyclerAdminUsers)

        val adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            countries
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        countrySpinner.adapter = adapter

        addTourButton.setOnClickListener {

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val date = Calendar.getInstance().apply {
                set(
                    datePicker.year,
                    datePicker.month,
                    datePicker.dayOfMonth
                )
            }

            if (addTitleText.text.toString().isNotEmpty() && addTourPrice.text.toString().isNotEmpty())
            {
                var newTour = CurrentTour(null, addTitleText.text.toString(), countrySpinner.selectedItem.toString(), dateFormat.format(date.time), addTourPrice.text.toString().toInt(), "", "")
                val db = MainDB.getDb(this.requireContext())
                Thread{
                    if (!db.getTourDao().tourExists(newTour.name))
                    {
                        db.getTourDao().insertItem(newTour)
                    }
                }.start()
                currentToursList.add(newTour)
                recyclerTours.adapter?.notifyItemInserted(currentToursList.size - 1)

            }
            else
            {
                Snackbar.make(addTourButton, "Все поля должны быть заполнены", Snackbar.LENGTH_SHORT).show()
            }
        }

        datePicker.minDate = Calendar.getInstance().timeInMillis
        datePicker.maxDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        val db = MainDB.getDb(this.requireContext())

        db.getTourDao().getAllItems().asLiveData().observe(this.requireContext() as LifecycleOwner) {

            currentToursList.clear()
            it.forEach {
                currentToursList.add(
                    CurrentTour(
                        it.id,
                        name = it.name,
                        country = it.country,
                        date = it.date,
                        price = it.price,
                        imageUrl = "",
                        flagUrl = ""
                    )
                )
            }

            recyclerTours.layoutManager = LinearLayoutManager(this.requireContext())
            recyclerTours.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int,
                ): RecyclerView.ViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_admin_tour, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

                    val titleText = holder.itemView.findViewById<TextView>(R.id.itemTitle)
                    val countryText = holder.itemView.findViewById<TextView>(R.id.itemCountry)
                    val dateText = holder.itemView.findViewById<TextView>(R.id.itemDate)
                    val priceText = holder.itemView.findViewById<TextView>(R.id.itemPrice)
                    val buttonEdit = holder.itemView.findViewById<Button>(R.id.edittourButton)
                    val buttonRemove = holder.itemView.findViewById<Button>(R.id.removetourButton)

                    titleText.setText("TITLE: ${currentToursList[position].name}")
                    countryText.setText("COUNTRY: ${currentToursList[position].country}")
                    dateText.setText("DATE: ${currentToursList[position].date}")
                    priceText.setText("PRICE: ${currentToursList[position].price}$")

                    buttonEdit.setOnClickListener {

                        val editTourFragment = EditTourFragment(currentToursList[position], user)

                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, editTourFragment)
                            .addToBackStack("")
                            .commit()
                    }

                    buttonRemove.setOnClickListener {
                        Thread{
                            db.getTourDao().delete(currentToursList[position])
                            db.getBoughtTourDao().deleteRemovedTour(currentToursList[position].name)
                        }.start()
                    }

                }

                override fun getItemCount() = currentToursList.size

            }
        }

        db.getUserDao().getAllItems().asLiveData().observe(this.requireContext() as LifecycleOwner) {

            usersList.clear()
            it.forEach {
                usersList.add(User(it.id, login = it.login, password = it.password, isAgent = it.isAgent, email = it.email))
            }

            recyclerUsers.layoutManager = LinearLayoutManager(this.requireContext())
            recyclerUsers.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int,
                ): RecyclerView.ViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_admin_user, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

                    val loginText = holder.itemView.findViewById<TextView>(R.id.userLoginText)
                    val passwordText = holder.itemView.findViewById<TextView>(R.id.userPasswordText)

                    val buttonRemove = holder.itemView.findViewById<Button>(R.id.removeUserButton)
                    val buttonEdit = holder.itemView.findViewById<Button>(R.id.editUserButton)

                    buttonRemove.setOnClickListener {
                        if (usersList[position].login != user.login)
                        {
                            Thread {
                                db.getUserDao().delete(usersList[position])
                                db.getBoughtTourDao().deleteRemovedUserTours(usersList[position].login)
                            }.start()
                        }
                        else
                        {
                            Snackbar.make(loginText, "Нельзя удалить себя", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    buttonEdit.setOnClickListener {

                        val editUserFragment = EditUserFragment(usersList[position], user)

                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, editUserFragment)
                            .addToBackStack("")
                            .commit()

                    }

                    loginText.setText(usersList[position].login)
                    passwordText.setText(usersList[position].password)

                }

                override fun getItemCount() = usersList.size

            }
        }

        return view
    }

}