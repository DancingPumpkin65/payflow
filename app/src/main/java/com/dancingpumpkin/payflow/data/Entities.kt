package com.dancingpumpkin.payflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_days")
data class WorkDayEntity(@PrimaryKey val date: String)

@Entity(tableName = "pay_rates")
data class PayRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val effectiveDate: String,
    val dailyAmountCents: Long,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val title: String,
    val amountCents: Long,
)
