package com.example.travel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTourFragment(var tour : CurrentTour, val user : User) : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_edit_tour, container, false)

        var titleEditText = view.findViewById<EditText>(R.id.editTourTitle)
        var priceEditText = view.findViewById<EditText>(R.id.editTourPrice)
        var countryText = view.findViewById<TextView>(R.id.editTourCountryText)
        var countrySpinner = view.findViewById<Spinner>(R.id.editTourCountry)
        var datePicker = view.findViewById<DatePicker>(R.id.editTourDatePicker)
        var buttonSave = view.findViewById<Button>(R.id.saveTour)

        val adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            countries
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        countrySpinner.adapter = adapter

        titleEditText.setText(tour.name)
        countryText.setText(tour.country)
        priceEditText.setText(tour.price.toString())

        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = format.parse(tour.date)

        val calendar = Calendar.getInstance()

        if (date != null) {
            calendar.time = date
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        datePicker.init(year, month, day, null)

        datePicker.minDate = Calendar.getInstance().timeInMillis
        datePicker.maxDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        buttonSave.setOnClickListener {
            if (titleEditText.text.toString().isNotEmpty() && priceEditText.text.toString().isNotEmpty())
            {

                val date = Calendar.getInstance().apply {
                    set(
                        datePicker.year,
                        datePicker.month,
                        datePicker.dayOfMonth
                    )
                }

                tour.name = titleEditText.text.toString()
                tour.country = countrySpinner.selectedItem.toString()
                tour.date = dateFormat.format(date.time)
                tour.price = priceEditText.text.toString().toInt()

                val db = MainDB.getDb(this.requireContext())
                Thread{
                    db.getTourDao().updateTour(tour)

                    val mainAdminFragment = AdminFragment(user)
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, mainAdminFragment)
                        .commit()

                }.start()
            }
        }

        return view
    }
}