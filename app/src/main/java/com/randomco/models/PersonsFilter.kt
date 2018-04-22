package com.randomco.models

data class PersonsFilter(
    private val textFilter: String? = null,
    private val distanceFilter: Float? = null,
    private val currentLocation: LatLon? = null,
    private val sortByGender: Boolean = false,
    private val sortByName: Boolean = false
) {
    operator fun invoke(persons: List<Person>): List<Person> {
        var out = persons

        out = if (textFilter != null) {
            out.filter {
                it.name.contains(textFilter, true)
                    || it.surname.contains(textFilter, true)
                    || it.email.contains(textFilter, true)
            }
        } else {
            out
        }

        out = if (distanceFilter != null && currentLocation != null) {
            out.filter {
                it.location.latLon distanceTo currentLocation < distanceFilter
            }
        } else {
            out
        }

        //TODO: Sort by both at the same time
        //Algorithm would be sort by gender, group genders into lists,
        //sort each gender group by name, and flatten into a list
        val sorted = when {
            sortByName -> out.sortedBy { it.name }
            sortByGender -> out.sortedBy { it.gender }
            else -> out
        }

        return sorted
    }
}