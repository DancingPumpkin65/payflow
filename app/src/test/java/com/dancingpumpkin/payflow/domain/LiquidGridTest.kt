package com.dancingpumpkin.payflow.domain

import kotlin.test.*

class LiquidGridTest {
    @Test fun `directly lower and upper days are visual neighbors`() {
        assertTrue(liquidGridNeighbors(8, 15))
        assertTrue(liquidGridNeighbors(15, 8))
        assertTrue(liquidGridNeighbors(8, 9))
        assertTrue(liquidGridNeighbors(8, 16))
    }
    @Test fun `week wrap and distant cells never form a long bridge`() {
        assertFalse(liquidGridNeighbors(6, 7))
        assertFalse(liquidGridNeighbors(8, 22))
        assertFalse(liquidGridNeighbors(8, 8))
        assertFalse(liquidGridNeighbors(-1, 0))
    }
}
