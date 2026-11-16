package com.dancingpumpkin.payflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "work_days")
data class WorkDayEntity(@PrimaryKey val date: String)

@Entity(tableName = "day_events")
data class DayEventEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val title: String, val notes: String,
    val time: String? = null, val reminderMinutes: Int? = null)

@Entity(tableName = "shop_income")
data class ShopIncomeEntity(@PrimaryKey val date: String, val amountCents: Long, val notes: String)

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
    @ColumnInfo(defaultValue = "'personal'") val category: String = "personal",
    @ColumnInfo(defaultValue = "'Uncategorized'") val spendingCategory: String = "Uncategorized",
)

@Entity(tableName = "savings_goals", primaryKeys = ["month", "ledger"])
data class SavingsGoalEntity(val month: String, val ledger: String, val targetCents: Long, val basis: String = "net")

@Entity(tableName = "savings_transfers")
data class SavingsTransferEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val ledger: String, val amountCents: Long)
