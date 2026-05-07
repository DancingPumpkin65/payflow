package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.*
import kotlin.test.*

class CalendarDetailsTest {
    @Test fun `markers include duplicate events and zero-income days with empty notes`() {
        val dates = calendarDetailDates(listOf(DayEventEntity(1, "2026-11-14", "One", ""), DayEventEntity(2, "2026-11-14", "Two", "")),
            listOf(ShopIncomeEntity("2026-11-20", 0, "")))
        assertEquals(setOf("2026-11-14", "2026-11-20"), dates)
        assertFalse("2026-11-15" in dates)
        assertTrue(calendarDetailDates(emptyList(), emptyList()).isEmpty())
    }
}
