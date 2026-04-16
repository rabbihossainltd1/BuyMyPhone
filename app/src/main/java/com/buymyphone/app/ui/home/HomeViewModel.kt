package com.buymyphone.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.BuyMyPhoneApplication
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BuyMyPhoneApplication).reportRepository

    private val _reportCount = MutableLiveData(0)
    val reportCount: LiveData<Int> = _reportCount

    init { fetchReportCount() }

    private fun fetchReportCount() {
        viewModelScope.launch {
            _reportCount.postValue(repository.getReportCount())
        }
    }

    fun refresh() = fetchReportCount()
}
