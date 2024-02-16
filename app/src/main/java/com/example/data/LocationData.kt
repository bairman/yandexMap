package com.example.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("title")
    val title: String,

    @SerializedName("subtitle")
    val subtitle: String?,

    @SerializedName("tags")
    val tags: List<String>,

    @SerializedName("address")
    val address: Address,

    @SerializedName("uri")
    val uri: String
)
//title: Название местоположения, например, "Элиста".
//subtitle: Дополнительная информация о местоположении. В вашем случае, она имеет тип JsonElement?,
// что позволяет хранить различные типы данных в формате JSON.
//tags: Список тегов, связанных с местоположением.
//address: Объект Address, представляющий адрес местоположения.
//uri: Строковое представление URI местоположения.