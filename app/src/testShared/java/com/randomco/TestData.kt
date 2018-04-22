package com.randomco

import com.randomco.models.*
import com.randomco.network.*


val anyNetworkPerson = NetworkPerson(
    gender = "male",
    name = NetworkName(first = "ritthy", last = "sanders", title = "mr"),
    location = NetworkLocation(
        street = "6676 springfield road",
        city = "wells",
        state = "staffordshire",
        postcode = "OX6C 2 BN"),
    email = "ritthy.sanders@example.com",
    login = NetworkLogin(username = "bigsnake650"),
    dob = "1982 - 08 - 22 09:16:27",
    registered = "2013 - 06-27 07:38:03",
    phone = "017684 89648", cell = "0735-393-842",
    picture = NetworkPicture(
        large = "https://randomuser.me/api/portraits/men/79.jpg",
        medium = "https ://randomuser.me/api/portraits/med/men/79.jpg",
        thumbnail = "https://randomuser.me/api/portraits/thumb/men/79.jpg")
)

val anyLatLon = LatLon(0f, 0f)

val anyPerson = Person(
    name = "ritthy",
    surname = "sanders",
    email = "ritthy.sanders@example.com",
    picture = Picture(
        thumb = "https://randomuser.me/api/portraits/thumb/men/79.jpg",
        big = "https://randomuser.me/api/portraits/men/79.jpg"),
    phone = "017684 89648",
    gender = Gender.MALE,
    location = Location(
        street = "6676 springfield road",
        city = "wells",
        state = "staffordshire",
        latLon = anyLatLon),
    favorite = false
)