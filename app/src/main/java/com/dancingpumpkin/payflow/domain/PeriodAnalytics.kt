package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.*
import java.time.*
import java.time.temporal.ChronoUnit

data class PeriodAnalytics(val income: Long, val expenses: Long, val categoryTotals: Map<String, Long>, val transfers: Long) {
    val net get() = income - expenses
    fun saved(basis: String) = if (basis == "transfers") transfers else net.coerceAtLeast(0)
    fun progress(target: Long, basis: String) = if (target <= 0) 0f else (saved(basis).toDouble() / target).coerceIn(0.0, 1.0).toFloat()
}

fun periodAnalytics(start: LocalDate, end: LocalDate, ledger: String, workDays: List<WorkDayEntity>, rates: List<PayRateEntity>, expenses: List<ExpenseEntity>, shop: List<ShopIncomeEntity>, transfers: List<SavingsTransferEntity>): PeriodAnalytics {
    require(!end.isBefore(start))
    val first = start.toString(); val last = end.toString()
    val spending = expenses.filter { it.category == ledger && it.date in first..last }
    val history = rates.sortedWith(compareBy<PayRateEntity> { it.effectiveDate }.thenBy { it.id })
    val income = if (ledger == "shop") shop.filter { it.date in first..last }.sumOf { it.amountCents }
        else workDays.filter { it.date in first..last }.sumOf { day -> history.lastOrNull { it.effectiveDate <= day.date }?.dailyAmountCents ?: 0 }
    return PeriodAnalytics(income, spending.sumOf { it.amountCents }, spending.groupBy { it.spendingCategory }.mapValues { (_, rows) -> rows.sumOf { it.amountCents } }.toList().sortedByDescending { it.second }.toMap(), transfers.filter { it.ledger == ledger && it.date in first..last }.sumOf { it.amountCents })
}

fun reminderInstant(event: DayEventEntity, zone: ZoneId): Instant? {
    val time = event.time ?: return null
    val minutes = event.reminderMinutes ?: return null
    return LocalDateTime.of(LocalDate.parse(event.date), LocalTime.parse(time)).atZone(zone).toInstant().minusSeconds(minutes * 60L)
}

/** Monday-first complete weeks, including leap days, with padding outside the year. */
fun activityWeeks(year: Int): List<List<LocalDate?>> {
    val first = LocalDate.of(year, 1, 1)
    val start = first.minusDays((first.dayOfWeek.value - 1).toLong())
    val last = LocalDate.of(year, 12, 31)
    val count = ((ChronoUnit.DAYS.between(start, last) + 7) / 7).toInt()
    return (0 until count).map { week -> (0..6).map { day -> start.plusDays(week * 7L + day).takeIf { it.year == year } } }
}
