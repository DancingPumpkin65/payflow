package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import com.dancingpumpkin.payflow.data.WorkDayEntity

data class Totals(val earnedCents: Long, val spentCents: Long) {
    val netCents: Long get() = earnedCents - spentCents
}

object IncomeCalculator {
    fun totals(workDays: List<WorkDayEntity>, rates: List<PayRateEntity>, expenses: List<ExpenseEntity>): Totals {
        val earned = workDays.sumOf { day -> rates.lastOrNull { it.effectiveDate <= day.date }?.dailyAmountCents ?: 0L }
        return Totals(earned, expenses.sumOf(ExpenseEntity::amountCents))
    }

    fun earnedForMonth(workDays: List<WorkDayEntity>, rates: List<PayRateEntity>, monthPrefix: String): Long =
        workDays.asSequence().filter { it.date.startsWith(monthPrefix) }.sumOf { day ->
            rates.lastOrNull { it.effectiveDate <= day.date }?.dailyAmountCents ?: 0L
        }
}
