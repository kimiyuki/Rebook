package com.abc_analytics.rebook.ui.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abc_analytics.rebook.R
import kotlinx.android.synthetic.main.fragment_capture.*

/**
 * A simple [Fragment] subclass.
 */
class CaptureFragment : Fragment() {

  private lateinit var captureViewModel: CaptureViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    captureViewModel = ViewModelProviders.of(this).get(CaptureViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_capture, container, false)
    captureViewModel.text.observe(this, Observer { text_capture.text = it })
    return root
  }

}
