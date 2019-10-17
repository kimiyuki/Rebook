package com.abc_analytics.rebook.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.abc_analytics.rebook.MainViewModel
import com.abc_analytics.rebook.MyApp
import com.abc_analytics.rebook.R
import com.abc_analytics.rebook.data.Book
import com.abc_analytics.rebook.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
  private val mainViewModel: MainViewModel by activityViewModels<MainViewModel>()
  private val viewModel: HomeViewModel by viewModels()
  private val ctx = MyApp.appContext

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val binding = DataBindingUtil.inflate<FragmentHomeBinding>(
                inflater, R.layout.fragment_home, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerViewHome.layoutManager = LinearLayoutManager(ctx)
    viewModel.books.value = createData()
    observeViewModel()
  }

  private fun observeViewModel() {
    viewModel.books.observe(this, Observer {
      recyclerViewHome.adapter = BookListAdapter(this@HomeFragment, viewModel)
    })
  }
}

fun createData(): List<Book> {
  return mutableListOf<Book>().apply {
    for (i in 1..100) {
      add(Book(i + 0, "java", listOf("mike", "kami")))
      add(Book(i + 1, "python", listOf("john", "kashy")))
    }
  }.toList()
}