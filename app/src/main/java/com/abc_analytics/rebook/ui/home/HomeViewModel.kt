package com.abc_analytics.rebook.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abc_analytics.rebook.data.Book

class HomeViewModel : ViewModel() {
  var books = MutableLiveData<List<Book>>()
}