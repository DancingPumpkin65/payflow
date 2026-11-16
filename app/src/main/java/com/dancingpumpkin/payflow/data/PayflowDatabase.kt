package com.dancingpumpkin.payflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WorkDayEntity::class, PayRateEntity::class, ExpenseEntity::class, DayEventEntity::class, ShopIncomeEntity::class, SavingsGoalEntity::class, SavingsTransferEntity::class], version = 4, exportSchema = false)
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
        }, object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN category TEXT NOT NULL DEFAULT 'personal'")
            }
        }, object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN spendingCategory TEXT NOT NULL DEFAULT 'Uncategorized'")
                db.execSQL("ALTER TABLE day_events ADD COLUMN time TEXT")
                db.execSQL("ALTER TABLE day_events ADD COLUMN reminderMinutes INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (month TEXT NOT NULL, ledger TEXT NOT NULL, targetCents INTEGER NOT NULL, basis TEXT NOT NULL, PRIMARY KEY(month, ledger))")
                db.execSQL("CREATE TABLE IF NOT EXISTS savings_transfers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, ledger TEXT NOT NULL, amountCents INTEGER NOT NULL)")
            }
        }).build()
    }
}
