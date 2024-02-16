package com.example.data

import com.google.gson.annotations.SerializedName

data class AddressComponent(
    @SerializedName("name")
    val name: String,

    @SerializedName("kind")
    val kind: List<String>
)
//name: Представляет название компонента адреса, например, "Россия" или "Элиста".
//kind: Список типов компонента адреса, таких как "COUNTRY" или "LOCALITY".
// Может содержать несколько значений, так как один компонент адреса может иметь несколько типов
// (например, "Элиста" может быть и "LOCALITY" и "ADMINISTRATIVE_AREA_LEVEL_2").