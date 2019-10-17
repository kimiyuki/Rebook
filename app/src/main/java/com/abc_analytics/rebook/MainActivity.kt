package com.abc_analytics.rebook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

class MainActivity : AppCompatActivity() {
  lateinit var navController: NavController
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Timber.d(BuildConfig.AWS_ACCESS_KEY)
    Timber.d("aaaa-bcd")
    val navView: BottomNavigationView = findViewById(R.id.nav_view)

    navController = findNavController(R.id.nav_host_fragment)
    // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
    val appBarConfiguration = AppBarConfiguration(
      setOf(
        R.id.navigation_home, R.id.navigation_scraps, R.id.navigation_detail,
        R.id.navigation_capture
      )
    )
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)
    observeViewModel()
  }

  private fun observeViewModel() {
    val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    mainViewModel.selectedBookId.observe(this, Observer {book_id ->
      Timber.d(book_id.toString())
      book_id?.let{ navController.navigate(R.id.navigation_scraps)}
    })
  }
}
