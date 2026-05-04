package com.dancingpumpkin.payflow.domain

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkDayRangeTest {
    @Test fun `includes skipped dates when dragging across weeks`() {
        assertEquals((6..16).map { "2026-11-${it.toString().padStart(2, '0')}" }, workDayRange(LocalDate.parse("2026-11-06"), LocalDate.parse("2026-11-16")))
    }
    @Test fun `dragging backwards spans month and leap year boundaries`() {
        assertEquals(listOf("2028-02-28", "2028-02-29", "2028-03-01"), workDayRange(LocalDate.parse("2028-03-01"), LocalDate.parse("2028-02-28")))
    }
    @Test fun `holding one date only selects that date`() {
        assertEquals(listOf("2026-11-14"), workDayRange(LocalDate.parse("2026-11-14"), LocalDate.parse("2026-11-14")))
    }
}
