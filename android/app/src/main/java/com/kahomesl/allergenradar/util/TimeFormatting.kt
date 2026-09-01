package com.kahomesl.allergenradar.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val localDateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm")

fun formatLocalTime(isoString: String?): String? = isoString?.let {
    runCatching {
        Instant.parse(it).atZone(ZoneId.systemDefault()).format(localDateTimeFormatter)
    }.getOrNull()
}

fun formatCachedTime(epochMillis: Long?): String? = epochMillis?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(localDateTimeFormatter)
}
