package com.randomco.models

import com.randomco.anyLatLon
import com.randomco.anyNetworkPerson
import com.randomco.anyPerson
import junit.framework.Assert.assertEquals
import org.junit.Test


class PersonMapperTest {
    @Test
    fun maps_correct_values() {
        val mapper = PersonMapper()
        val mappedPerson = mapper(anyNetworkPerson)
        val mappedWithoutLatLon = mappedPerson.copy(location = mappedPerson.location.copy(latLon = anyLatLon))
        assertEquals(anyPerson, mappedWithoutLatLon)
    }

}