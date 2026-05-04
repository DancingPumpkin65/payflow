package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.*
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthlyStatsTest {
    @Test fun `personal and shop expenses stay in separate ledgers`() {
        val expenses = listOf(ExpenseEntity(1, "2026-11-15", "Personal", 1234), ExpenseEntity(2, "2026-11-15", "Shop", 5678, "shop"))
        val stats = monthlyStats(YearMonth.of(2026, 11), emptyList(), emptyList(), expenses, emptyList())
        assertEquals(1234L, stats.expenses)
        assertEquals(5678L, stats.shopExpenses)
        assertEquals(1234L, IncomeCalculator.totals(emptyList(), emptyList(), expenses).spentCents)
    }
    @Test fun `monthly data respects date boundaries and historical rates without mixing shop revenue`() {
        val stats = monthlyStats(YearMonth.of(2026, 11),
            listOf(WorkDayEntity("2026-10-31"), WorkDayEntity("2026-11-01"), WorkDayEntity("2026-11-15")),
            listOf(PayRateEntity(2, "2026-11-15", 12345), PayRateEntity(1, "2026-10-01", 10000)),
            listOf(ExpenseEntity(1, "2026-11-15", "One", 1234), ExpenseEntity(2, "2026-11-15", "Two", 1), ExpenseEntity(3, "2026-12-01", "Outside", 99999)),
            listOf(ShopIncomeEntity("2026-11-15", 20000, "")))
        assertEquals(22345L, stats.workPay)
        assertEquals(20000L, stats.revenue)
        assertEquals(1235L, stats.expenses)
        assertEquals(12345L, stats.days[14].workPay)
        assertEquals(30, stats.days.size)
    }

    @Test fun `leap month includes last day and allows a negative net with no income`() {
        val stats = monthlyStats(YearMonth.of(2024, 2), emptyList(), emptyList(),
            listOf(ExpenseEntity(1, "2024-02-29", "Expense", 500)), emptyList())
        assertEquals(29, stats.days.size)
        assertEquals(-500L, stats.revenue - stats.expenses)
        assertEquals(500L, stats.days.last().expenses)
    }

    @Test fun `empty month has zero totals and no invented chart values`() {
        val stats = monthlyStats(YearMonth.of(2026, 1), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(31, stats.days.size)
        assertEquals(0L, stats.workPay + stats.revenue + stats.expenses)
    }
}
