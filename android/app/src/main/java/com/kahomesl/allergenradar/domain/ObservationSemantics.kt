package com.kahomesl.allergenradar.domain

import com.kahomesl.allergenradar.data.ObservationDto

const val ARTEMISIA_TAXON = "ARTEMISIA"

fun selectPrimaryTotal(observations: List<ObservationDto>): ObservationDto? = observations
    .filter { it.scope == "TOTAL" }
    .sortedWith(compareBy { measurementPriority(it.measurementType) })
    .firstOrNull()

fun selectArtemisia(observations: List<ObservationDto>): ObservationDto? = observations
    .firstOrNull { it.taxon?.code.equals(ARTEMISIA_TAXON, ignoreCase = true) }

private fun measurementPriority(type: String): Int = when (type) {
    "CURRENT" -> 0
    "FORECAST" -> 1
    "OBSERVATION" -> 2
    "ESTIMATE" -> 3
    else -> 4
}
