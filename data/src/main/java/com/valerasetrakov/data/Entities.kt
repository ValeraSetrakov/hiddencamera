package com.valerasetrakov.data

import com.example.common.defaultNow
import com.google.gson.annotations.SerializedName

data class Box (
    val id: String,
    val name: String,
    val mobiles: List<MobileDevice>
)

data class MobileDevice (
    @SerializedName("phone_id") val phoneId: String,
    val siren: String? = "",
    @SerializedName("road_min") val roadMin: Int = 0,
    val users: List<User>? = emptyList(),
    val mobiles: List<Mobile>? = emptyList()
)

data class User (
    @SerializedName("phone_number") val phoneNumber: String,
    val email: String
)

data class Mobile (
    @SerializedName("phone_id") val phoneId: String,
    @SerializedName("box_id") val boxId: Int
)

data class MobileState (
    val device: String,
    @SerializedName("mark_time") var markTime: String = defaultNow(),
    val location: Location,
    @SerializedName("charge_level") var chargeLevel: Int = 0,
    @SerializedName("free_memory") var freeMemory: Long = 0,
    @SerializedName("is_start_geopoint") var isStartGeoPoint: Boolean = false,
    @SerializedName("is_end_geopoint") var isEndGeoPoint: Boolean = false
)

data class VideoFragment (
    var device: String,
    var video: String,
    @SerializedName("charge_level") var chargeLevel: Int = 0,
    @SerializedName("mark_time") var markTime: String = defaultNow(),
    @SerializedName("free_memory") var freeMemory: Long = 0,
    @SerializedName("is_start_frame") var isStartFrame: Boolean = false,
    @SerializedName("is_end_frame") var isEndFrame: Boolean = false
)

data class Location (
    val type: String = "Point",
    val coordinates: DoubleArray
)