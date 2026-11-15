package com.dancingpumpkin.payflow.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** A drag selects every calendar date between its anchor and current endpoint. */
internal fun workDayRange(anchor: LocalDate, endpoint: LocalDate, month: YearMonth? = null): List<String> {
    val first = minOf(anchor, endpoint)
    val last = maxOf(anchor, endpoint)
    return (0..ChronoUnit.DAYS.between(first, last)).map { first.plusDays(it) }.filter { month == null || YearMonth.from(it) == month }.map { it.toString() }
}
