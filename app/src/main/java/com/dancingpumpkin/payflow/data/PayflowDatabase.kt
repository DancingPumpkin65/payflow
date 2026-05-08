package com.dancingpumpkin.payflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WorkDayEntity::class, PayRateEntity::class, ExpenseEntity::class, DayEventEntity::class, ShopIncomeEntity::class], version = 2, exportSchema = false)
abstract class PayflowDatabase : RoomDatabase() {
    abstract fun dao(): PayflowDao

    companion object {
        fun create(context: Context): PayflowDatabase = Room.databaseBuilder(
            context.applicationContext,
            PayflowDatabase::class.java,
            "payflow.db",
        ).addMigrations(object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS day_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS shop_income (date TEXT PRIMARY KEY NOT NULL, amountCents INTEGER NOT NULL, notes TEXT NOT NULL)")
            }
        }).build()
    }
}
