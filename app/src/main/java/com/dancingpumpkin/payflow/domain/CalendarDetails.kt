package com.dancingpumpkin.payflow.domain

import com.dancingpumpkin.payflow.data.DayEventEntity
import com.dancingpumpkin.payflow.data.ShopIncomeEntity

internal fun calendarDetailDates(events: List<DayEventEntity>, shop: List<ShopIncomeEntity>): Set<String> = buildSet {
    events.forEach { add(it.date) }
    shop.forEach { add(it.date) }
}
