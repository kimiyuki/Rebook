package com.abc_analytics.rebook.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abc_analytics.rebook.R

class DetailFragment : Fragment() {

  private lateinit var detailViewModel: DetailViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    detailViewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_detail, container, false)
    val textView: TextView = root.findViewById(R.id.text_notifications)
    detailViewModel.text.observe(this, Observer { textView.text = it })
    return root
  }
}