package com.abc_analytics.rebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainViewModel(private val context: Application) : AndroidViewModel(context) {
  var text = MutableLiveData<String>()

  init {
    text.value = ""
  }
}