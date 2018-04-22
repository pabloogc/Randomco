package com.randomco.models

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LatLonTest {

    private fun isClose(target: Float, actual: Float) {
        //30%, we don't care that much about accuracy
        val isClose = abs(target - actual) < target * 0.3f
        return assertTrue("Target value: $target, actual distance $actual", isClose)
    }

    @Test
    fun distances_are_within_error_margins() {
        //Distances are from GoogleMaps
        val a = LatLon(40.4742675f, -3.68750f)
        val b = LatLon(40.4741015f, -3.68681f)
        isClose(0.06f, a distanceTo b) //a - b are ~60 meters apart

        val c = LatLon(40.4323089f, -3.717562f)
        val d = LatLon(40.4295267f, -3.715498f)
        val e = LatLon(40.4279319f, -3.696077f)
        isClose(0.35f, c distanceTo d) //c - d are ~350 meters apart
        isClose(2.0f, c distanceTo e) //c - e are ~2000 meters apart
    }
}