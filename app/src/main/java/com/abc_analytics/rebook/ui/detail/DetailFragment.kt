package com.abc_analytics.rebook.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abc_analytics.rebook.R
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {

  private lateinit var detailViewModel: DetailViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_detail, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    detailViewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
    detailViewModel.text.observe(this, Observer { text_notifications.text = it })
  }
}