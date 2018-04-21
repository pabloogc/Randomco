package com.randomco.network

data class NetworkPersonList(
    val results: List<NetworkPerson>
)

data class NetworkPerson(
    val gender: String,
    val name: NetworkName,
    val location: NetworkLocation,
    val email: String,
    val login: NetworkLogin,
    val dob: String,
    val registered: String,
    val phone: String,
    val cell: String,
    val picture: NetworkPicture
)

data class NetworkPicture(
    val large: String,
    val medium: String,
    val thumbnail: String
)

data class NetworkName(
    val first: String,
    val last: String,
    val title: String
)

data class NetworkLogin(
    val username: String
)

data class NetworkLocation(
    val street: String,
    val city: String,
    val state: String,
    val postcode: String
)
