package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import com.dancingpumpkin.payflow.data.WorkDayEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class IncomeCalculatorTest {
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
}
