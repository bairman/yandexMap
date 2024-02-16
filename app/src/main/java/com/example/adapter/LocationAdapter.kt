package com.example.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.data.LocationData
import com.example.yandexmap.R

class LocationAdapter(context: Context, locations: List<LocationData>)
    : ArrayAdapter<LocationData>(context, 0, locations) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
        val locationData = getItem(position)

        val textViewCity = view.findViewById<TextView>(R.id.textViewCity)
        val textViewRegionCountry = view.findViewById<TextView>(R.id.textViewRegionCountry)

        textViewCity.text = locationData?.city
        textViewRegionCountry.text = "${locationData?.region}, ${locationData?.country}"

        return view
    }
}
