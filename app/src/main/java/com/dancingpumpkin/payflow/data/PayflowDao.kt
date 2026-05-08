package com.dancingpumpkin.payflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PayflowDao {
    @Query("SELECT * FROM day_events ORDER BY date, id") fun observeEvents(): Flow<List<DayEventEntity>>
    @Insert suspend fun addEvent(event: DayEventEntity)
    @Delete suspend fun deleteEvent(event: DayEventEntity)
    @Query("SELECT * FROM shop_income ORDER BY date") fun observeShopIncome(): Flow<List<ShopIncomeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveShopIncome(income: ShopIncomeEntity)
    @Query("SELECT EXISTS(SELECT 1 FROM work_days WHERE date = :date)") suspend fun isWorked(date: String): Boolean
    @Transaction
    suspend fun toggleWorkDay(date: String) {
        if (isWorked(date)) removeWorkDay(date) else addWorkDay(WorkDayEntity(date))
    }
    @Query("SELECT * FROM work_days ORDER BY date") fun observeWorkDays(): Flow<List<WorkDayEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addWorkDay(day: WorkDayEntity)
    @Query("DELETE FROM work_days WHERE date = :date") suspend fun removeWorkDay(date: String)

    @Query("SELECT * FROM pay_rates ORDER BY effectiveDate") fun observePayRates(): Flow<List<PayRateEntity>>
    @Insert suspend fun addPayRate(rate: PayRateEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC") fun observeExpenses(): Flow<List<ExpenseEntity>>
    @Insert suspend fun addExpense(expense: ExpenseEntity)
    @Update suspend fun updateExpense(expense: ExpenseEntity)
    @Delete suspend fun deleteExpense(expense: ExpenseEntity)
}
