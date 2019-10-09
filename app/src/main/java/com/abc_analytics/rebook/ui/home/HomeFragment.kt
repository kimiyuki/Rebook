package com.abc_analytics.rebook.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abc_analytics.rebook.MainViewModel
import com.abc_analytics.rebook.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

  lateinit var viewModel: MainViewModel
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
    viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    viewModel.text.observe(this, Observer {
      text_home.text = it
    })
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    button_home_frag.setOnClickListener {
      viewModel.setMyText()
    }
  }
}