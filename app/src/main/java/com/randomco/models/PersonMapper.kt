package com.randomco.models

import com.randomco.network.NetworkPerson


class PersonMapper {
    operator fun invoke(networkPerson: NetworkPerson): Person {
        return Person(
            name = networkPerson.name.first,
            surname = networkPerson.name.last,
            email = networkPerson.email,
            picture = Picture(
                big = networkPerson.picture.large,
                thumb = networkPerson.picture.thumbnail
            ),
            phone = networkPerson.phone,
            gender = when (networkPerson.gender) {
                "male" -> Gender.MALE
                "female" -> Gender.FEMALE
                else -> Gender.OTHER
            },
            location = Location(
                street = networkPerson.location.street,
                state = networkPerson.location.state,
                city = networkPerson.location.city,
                latLon = generateRandomLocation()
            ),
            favorite = false
        )
    }

    private fun generateRandomLocation(): LatLon {
        val madrid = LatLon(40.4742675f, -3.68750f)
        val distanceFactor = if (Math.random() > 0.5) {
            0.00001f //Generate a really close one
        } else {
            1f //Any location
        }

        val deviation = 40 //go from -deviation to +deviation
        val extraLat = Math.random().toFloat() * deviation * 2 - deviation
        val extraLon = Math.random().toFloat() * deviation * 2 - deviation

        return LatLon(
            latitude = madrid.latitude + (extraLat + distanceFactor),
            longitude = madrid.longitude + (extraLon + distanceFactor)
        )
    }
}