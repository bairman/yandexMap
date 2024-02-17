package com.example.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.data.LocationData
import com.example.yandexmap.R

class LocationAdapter(context: Context, locations: List<String>)
    : ArrayAdapter<String>(context, 0, locations) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
        val location = getItem(position) // Здесь location уже строка

        val textViewCity = view.findViewById<TextView>(R.id.textViewCity)
        // Здесь мы просто используем строку, так как location уже строковое представление
        val textViewCountry = view.findViewById<TextView>(R.id.textViewRegionCountry)
        val parts = location?.split(", ") // Допустим, что каждый элемент списка содержит данные в формате "Город, Регион, Страна"
        textViewCity.text = parts?.getOrNull(0) // Устанавливаем название города
        textViewCountry.text = parts?.getOrNull(1)

        return view
    }
}

