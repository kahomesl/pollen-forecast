package com.kahomesl.allergenradar.location

import com.kahomesl.allergenradar.data.LocationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedLocationMatcherTest {
    private val matcher = SupportedLocationMatcher()

    @Test
    fun matchesBeijingCityAtItsCanonicalPointWithZeroDistance() {
        val match = matcher.match(39.9042, 116.4074, locations())

        requireNotNull(match)
        assertEquals("cn-city-beijing", match.location.id)
        assertEquals(0.0, match.distanceKm, 0.0001)
    }

    @Test
    fun prefersChaoyangWhenItsDistrictPointIsClearlyNearerThanBeijing() {
        val match = matcher.match(39.9215, 116.4864, locations())

        requireNotNull(match)
        assertEquals("cn-beijing-chaoyang", match.location.id)
    }

    @Test
    fun keepsBeijingCityWhenDistrictIsNotClearlyNearer() {
        val match = matcher.match(39.9042, 116.4074, locations())

        requireNotNull(match)
        assertEquals("cn-city-beijing", match.location.id)
    }

    @Test
    fun matchesXianAndShanghaiCanonicalPoints() {
        val xian = matcher.match(34.3416, 108.9398, locations())
        val shanghai = matcher.match(31.2304, 121.4737, locations())

        assertEquals("cn-city-xian", xian?.location?.id)
        assertEquals("cn-city-shanghai", shanghai?.location?.id)
    }

    @Test
    fun returnsUnsupportedWhenClosestSupportedLocationIsTooFarAway() {
        assertNull(matcher.match(40.7128, -74.0060, locations()))
    }

    @Test
    fun ignoresLocationsWithoutBothCoordinatesAndHandlesEmptyLists() {
        val missingCoordinates = LocationDto("missing", "缺失坐标", "CITY", latitude = null, longitude = 116.0)

        assertNull(matcher.match(39.9, 116.4, emptyList()))
        val match = matcher.match(39.9, 116.4, listOf(missingCoordinates, locations().first()))
        assertEquals("cn-city-beijing", match?.location?.id)
    }

    @Test
    fun exposesTheConfiguredMaximumDistanceForUserFacingPolicyDocumentation() {
        assertEquals(150.0, SupportedLocationMatcher.MAXIMUM_DISTANCE_KM, 0.0)
        assertTrue(matcher.match(39.9042, 116.4074, locations())!!.distanceKm <= SupportedLocationMatcher.MAXIMUM_DISTANCE_KM)
    }

    private fun locations() = listOf(
        LocationDto("cn-city-beijing", "北京", "CITY", 39.9042, 116.4074),
        LocationDto("cn-beijing-chaoyang", "朝阳区", "DISTRICT", 39.9215, 116.4864),
        LocationDto("cn-city-xian", "西安", "CITY", 34.3416, 108.9398),
        LocationDto("cn-city-shanghai", "上海", "CITY", 31.2304, 121.4737),
    )
}
