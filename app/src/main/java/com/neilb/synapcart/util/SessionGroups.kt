package com.neilb.synapcart.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Relative-date buckets for the chat history drawer. */
enum class SessionGroup { TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, OLDER }

fun sessionGroupOf(
    createdAtMillis: Long?,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault()
): SessionGroup {
    if (createdAtMillis == null) return SessionGroup.OLDER
    val day: LocalDate = Instant.ofEpochMilli(createdAtMillis).atZone(zone).toLocalDate()
    val today: LocalDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(day, today)
    return when {
        days <= 0L -> SessionGroup.TODAY
        days == 1L -> SessionGroup.YESTERDAY
        days < 7L -> SessionGroup.LAST_7_DAYS
        days < 30L -> SessionGroup.LAST_30_DAYS
        else -> SessionGroup.OLDER
    }
}
