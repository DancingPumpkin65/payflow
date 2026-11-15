package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.*
import java.time.YearMonth

data class DailyStats(val day: Int, val workPay: Long, val revenue: Long, val expenses: Long)
data class MonthlyStats(val days: List<DailyStats>) {
    val workPay = days.sumOf { it.workPay }
    val revenue = days.sumOf { it.revenue }
    val expenses = days.sumOf { it.expenses }
}

fun monthlyStats(month: YearMonth, workDays: List<WorkDayEntity>, rates: List<PayRateEntity>, expenses: List<ExpenseEntity>, shopIncome: List<ShopIncomeEntity>): MonthlyStats {
    val worked = workDays.map { it.date }.toSet()
    val history = rates.sortedWith(compareBy<PayRateEntity> { it.effectiveDate }.thenBy { it.id })
    val spending = expenses.groupBy { it.date }.mapValues { (_, rows) -> rows.sumOf { it.amountCents } }
    val revenue = shopIncome.associate { it.date to it.amountCents }
    return MonthlyStats((1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day).toString()
        DailyStats(day, if (date in worked) history.lastOrNull { it.effectiveDate <= date }?.dailyAmountCents ?: 0 else 0,
            revenue[date] ?: 0, spending[date] ?: 0)
    })
}
