package com.dancingpumpkin.payflow.domain

/** Visual neighbors, not consecutive dates: row wrapping must not draw a long neck. */
internal fun liquidGridNeighbors(from: Int, to: Int): Boolean = from in 0..41 && to in 0..41 && from != to &&
    kotlin.math.abs(from / 7 - to / 7) <= 1 && kotlin.math.abs(from % 7 - to % 7) <= 1
