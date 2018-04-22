package com.randomco.models

data class Person(
    val name: String,
    val surname: String,
    val email: String,
    val picture: Picture,
    val phone: String,
    val gender: Gender,
    val favorite: Boolean,
    val location: Location
) {
    /**
     * Unique identifier for a person, let's assume the email works
     */
    val id get() = email
}

enum class Gender {
    MALE, FEMALE, OTHER //ATTACK_HELICOPTER?
}

data class Location(
    val street: String,
    val city: String,
    val state: String,
    val latLon: LatLon
)

data class LatLon(val latitude: Float, val longitude: Float) {
    companion object {
        private const val EARTH_RAD_KM = 6371
        val MADRID = LatLon(40.4742675f, -3.68750f)
    }

    /**
     * Somewhat accurate distanceTo calculation, from
     * https://www.movable-type.co.uk/scripts/latlong.html
     */
    infix fun distanceTo(other: LatLon): Float {
        fun toRad(deg: Float) = deg * (Math.PI / 180)

        val lat1 = this.latitude
        val lat2 = other.latitude
        val lon1 = this.longitude
        val lon2 = other.longitude
        val dLat = toRad(lat2 - lat1)
        val dLon = toRad(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (EARTH_RAD_KM * c).toFloat()
    }
}

data class Picture(
    val thumb: String,
    val big: String
)