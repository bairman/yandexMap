package com.example.data

import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("formatted_address")
    val formattedAddress: String,

    @SerializedName("component")
    val component: List<AddressComponent>
)
//formattedAddress: Строковое представление форматированного адреса, например, "Республика Калмыкия, Элиста".
//component: Список компонентов адреса, представленных в виде объектов AddressComponent.
// Это позволяет представлять сложные адреса с различными компонентами (например, "Россия" как страна и "Элиста"
// как населенный пункт).