package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import com.dancingpumpkin.payflow.data.WorkDayEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class IncomeCalculatorTest {
    @Test fun `one rate applies to every subsequent worked day including weekends`() {
        val totals = IncomeCalculator.totals(
            listOf(WorkDayEntity("2026-01-02"), WorkDayEntity("2026-01-03"), WorkDayEntity("2026-01-04")),
            listOf(PayRateEntity(1, "2026-01-01", 15_025)), emptyList(),
        )
        assertEquals(45_075, totals.earnedCents)
    }

    @Test fun `uses each effective rate including the rate change day`() {
        val rates = listOf(PayRateEntity(1, "2026-01-01", 15_000), PayRateEntity(2, "2026-04-01", 17_500))
        val worked = listOf(WorkDayEntity("2026-03-20"), WorkDayEntity("2026-04-01"), WorkDayEntity("2026-04-08"))
        assertEquals(50_000, IncomeCalculator.totals(worked, rates, emptyList()).earnedCents)
    }

    @Test fun `expenses lower net without changing income`() {
        val totals = IncomeCalculator.totals(
            listOf(WorkDayEntity("2026-01-02")), listOf(PayRateEntity(1, "2026-01-01", 15_000)),
            listOf(ExpenseEntity(1, "2026-01-02", "Lunch", 3_500)),
        )
        assertEquals(15_000, totals.earnedCents)
        assertEquals(11_500, totals.netCents)
    }

    @Test fun `removing a worked day recalculates earned income`() {
        val days = listOf(WorkDayEntity("2026-01-02"), WorkDayEntity("2026-01-03"))
        val rates = listOf(PayRateEntity(1, "2026-01-01", 15_000))
        assertEquals(30_000, IncomeCalculator.totals(days, rates, emptyList()).earnedCents)
        assertEquals(15_000, IncomeCalculator.totals(days.dropLast(1), rates, emptyList()).earnedCents)
    }

    @Test fun `editing and deleting expenses changes only spent and net`() {
        val days = listOf(WorkDayEntity("2026-01-02"))
        val rates = listOf(PayRateEntity(1, "2026-01-01", 15_000))
        val original = ExpenseEntity(1, "2026-01-02", "Lunch", 3_500)
        val edited = IncomeCalculator.totals(days, rates, listOf(original.copy(amountCents = 4_025)))
        assertEquals(15_000, edited.earnedCents)
        assertEquals(4_025, edited.spentCents)
        assertEquals(10_975, edited.netCents)
        val deleted = IncomeCalculator.totals(days, rates, emptyList())
        assertEquals(15_000, deleted.earnedCents)
        assertEquals(0, deleted.spentCents)
        assertEquals(15_000, deleted.netCents)
    }

    @Test fun `monthly income excludes other months and days before the first rate`() {
        val days = listOf(WorkDayEntity("2025-12-31"), WorkDayEntity("2026-01-01"), WorkDayEntity("2026-01-02"), WorkDayEntity("2026-02-01"))
        val rates = listOf(PayRateEntity(1, "2026-01-02", 15_000))
        assertEquals(15_000, IncomeCalculator.earnedForMonth(days, rates, "2026-01"))
        assertEquals(30_000, IncomeCalculator.totals(days, rates, emptyList()).earnedCents)
    }
}
