package com.dancingpumpkin.payflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkDayEntity::class, PayRateEntity::class, ExpenseEntity::class], version = 1, exportSchema = false)
abstract class PayflowDatabase : RoomDatabase() {
    abstract fun dao(): PayflowDao

    companion object {
        fun create(context: Context): PayflowDatabase = Room.databaseBuilder(
            context.applicationContext,
            PayflowDatabase::class.java,
            "payflow.db",
        ).build()
    }
}
