package com.dancingpumpkin.payflow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import com.dancingpumpkin.payflow.data.PayflowDatabase
import com.dancingpumpkin.payflow.data.WorkDayEntity
import com.dancingpumpkin.payflow.data.DayEventEntity
import com.dancingpumpkin.payflow.data.ShopIncomeEntity
import com.dancingpumpkin.payflow.domain.IncomeCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PayflowState(
    val workDays: List<WorkDayEntity> = emptyList(),
    val rates: List<PayRateEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val events: List<DayEventEntity> = emptyList(),
    val shopIncome: List<ShopIncomeEntity> = emptyList(),
)

class PayflowViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PayflowDatabase.create(application).dao()
    val state = combine(dao.observeWorkDays(), dao.observePayRates(), dao.observeExpenses(), dao.observeEvents(), dao.observeShopIncome()) { days, rates, expenses, events, shop ->
        PayflowState(days, rates, expenses, events, shop)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PayflowState())

    fun toggleWorkDay(date: String) = viewModelScope.launch {
        dao.toggleWorkDay(date)
    }
    fun addRate(date: String, amountCents: Long) = viewModelScope.launch { dao.addPayRate(PayRateEntity(effectiveDate = date, dailyAmountCents = amountCents)) }
    fun addExpense(date: String, title: String, amountCents: Long) = viewModelScope.launch { dao.addExpense(ExpenseEntity(date = date, title = title, amountCents = amountCents)) }
    fun deleteExpense(expense: ExpenseEntity) = viewModelScope.launch { dao.deleteExpense(expense) }
    fun updateExpense(expense: ExpenseEntity) = viewModelScope.launch { dao.updateExpense(expense) }
    fun addEvent(event: DayEventEntity) = viewModelScope.launch { dao.addEvent(event) }
    fun deleteEvent(event: DayEventEntity) = viewModelScope.launch { dao.deleteEvent(event) }
    fun saveShopIncome(income: ShopIncomeEntity) = viewModelScope.launch { dao.saveShopIncome(income) }
    fun earnedForMonth(month: String) = IncomeCalculator.earnedForMonth(state.value.workDays, state.value.rates, month)
    fun totals() = IncomeCalculator.totals(state.value.workDays, state.value.rates, state.value.expenses)
}
