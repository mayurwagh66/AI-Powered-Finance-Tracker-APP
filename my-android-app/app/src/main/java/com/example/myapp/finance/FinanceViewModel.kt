package com.example.myapp.finance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapp.finance.GeminiClient

class FinanceViewModel(private val geminiClient: GeminiClient) : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String> get() = _response

    fun fetchSpendingAndSaving(query: String) {
        // Call the GeminiClient to fetch data based on the user's query
        geminiClient.getSpendingAndSaving(query) { result ->
            _response.postValue(result)
        }
    }
}