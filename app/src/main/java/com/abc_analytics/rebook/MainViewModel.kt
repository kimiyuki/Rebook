package com.abc_analytics.rebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.abc_analytics.rebook.data.Book
import java.util.*

class MainViewModel(private val context: Application) : AndroidViewModel(context) {
  var selectedBookId = MutableLiveData<Int?>()
  var text = MutableLiveData<String>()
  var book_title = MutableLiveData<String>()
  var book_author = MutableLiveData<List<String>>()

  init {
    selectedBookId.value = null
    text.value = ""
    book_title.value = ""
    book_author.value = listOf()
  }
}