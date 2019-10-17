package com.abc_analytics.rebook

import android.app.Application
import android.content.Context
import com.abc_analytics.rebook.data.Book
import timber.log.Timber


class MyApp : Application() {
  companion object {
    lateinit var appContext: Context
  }

  override fun onCreate() {
    super.onCreate()
    appContext = this
//        startKoin{
//            androidLogger()
//            androidContext(this@MyApplication)
//            modules(appModule)
//        }
//        themeList = setThemeData()
    Timber.plant(Timber.DebugTree())
  }
}

