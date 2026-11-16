package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.*
import java.time.*
import kotlin.test.*

class PeriodAnalyticsTest {
    @Test fun `custom period is inclusive and separates ledgers and custom categories`() {
        val data = periodAnalytics(LocalDate.parse("2026-10-31"), LocalDate.parse("2026-11-15"), "personal",
            listOf(WorkDayEntity("2026-10-31"), WorkDayEntity("2026-11-15"), WorkDayEntity("2026-11-16")),
            listOf(PayRateEntity(1, "2026-01-01", 10000), PayRateEntity(2, "2026-11-01", 12000)),
            listOf(ExpenseEntity(1, "2026-10-31", "Bus", 1234, "personal", "Transport"), ExpenseEntity(2, "2026-11-15", "Book", 500, "personal", "Learning"), ExpenseEntity(3, "2026-11-15", "Stock", 9999, "shop", "Shopping"), ExpenseEntity(4, "2026-11-16", "Later", 500, "personal", "Food")),
            emptyList(), emptyList())
        assertEquals(22000L, data.income)
        assertEquals(1734L, data.expenses)
        assertEquals(mapOf("Transport" to 1234L, "Learning" to 500L), data.categoryTotals)
        assertEquals(data.expenses, data.categoryTotals.values.sum())
    }
    @Test fun `savings transfers neither reduce net nor add to it`() {
        val data = periodAnalytics(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"), "shop", emptyList(), emptyList(),
            listOf(ExpenseEntity(1, "2026-11-15", "Stock", 2000, "shop", "Shopping")),
            listOf(ShopIncomeEntity("2026-11-15", 10000, "")),
            listOf(SavingsTransferEntity(1, "2026-11-15", "shop", 3000), SavingsTransferEntity(2, "2026-11-15", "personal", 9999)))
        assertEquals(8000L, data.net)
        assertEquals(3000L, data.saved("transfers"))
        assertEquals(8000L, data.saved("net"))
        assertEquals(.3f, data.progress(10000, "transfers"))
        assertEquals(1f, data.progress(5000, "net"))
    }
    @Test fun `negative net and absent goal never create positive savings`() {
        val data = PeriodAnalytics(0, 2000, mapOf("Food" to 2000), 0)
        assertEquals(-2000L, data.net)
        assertEquals(0L, data.saved("net"))
        assertEquals(0f, data.progress(10000, "net"))
        assertEquals(0f, data.progress(0, "net"))
    }
    @Test fun `reversed period is rejected`() {
        assertFailsWith<IllegalArgumentException> { periodAnalytics(LocalDate.parse("2026-11-02"), LocalDate.parse("2026-11-01"), "personal", emptyList(), emptyList(), emptyList(), emptyList(), emptyList()) }
    }
    @Test fun `heatmap includes each leap year date exactly once and pads outside year`() {
        val weeks = activityWeeks(2012)
        val dates = weeks.flatten().filterNotNull()
        assertEquals(54, weeks.size)
        assertEquals(366, dates.size)
        assertEquals(366, dates.toSet().size)
        assertEquals(LocalDate.parse("2012-01-01"), dates.first())
        assertEquals(LocalDate.parse("2012-12-31"), dates.last())
        assertTrue(LocalDate.parse("2012-02-29") in dates)
    }
    @Test fun `reminder uses local event time including DST gaps and lead time`() {
        val event = DayEventEntity(1, "2024-03-31", "Event", "", "02:30", 10)
        assertEquals(Instant.parse("2024-03-31T01:20:00Z"), reminderInstant(event, ZoneId.of("Europe/Paris")))
        assertNull(reminderInstant(event.copy(time = null), ZoneOffset.UTC))
        assertNull(reminderInstant(event.copy(reminderMinutes = null), ZoneOffset.UTC))
    }
}
