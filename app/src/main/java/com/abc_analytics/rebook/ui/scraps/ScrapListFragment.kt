package com.abc_analytics.rebook.ui.scraps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abc_analytics.rebook.R

class ScrapListFragment : Fragment() {

    private lateinit var scrapListViewModel: ScrapListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scrapListViewModel =
                ViewModelProviders.of(this).get(ScrapListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_scrap, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        scrapListViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}