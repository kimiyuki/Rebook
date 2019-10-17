package com.abc_analytics.rebook.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.abc_analytics.rebook.R
import com.abc_analytics.rebook.databinding.BookListBinding

class BookListAdapter(val fragment: Fragment, val viewModel: HomeViewModel)
  : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

  inner class BookViewHolder(val binding: BookListBinding) : RecyclerView.ViewHolder(binding.root) {}

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
    val binding = DataBindingUtil.inflate<BookListBinding>(
                LayoutInflater.from(parent.context), R.layout.book_list, parent, false)
    return BookViewHolder(binding)
  }

  override fun getItemCount() = viewModel.books.value?.size ?: 0

  override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
    holder.binding.viewmodel = viewModel
    holder.binding.position = position
    holder.binding.book = viewModel.books.value?.get(position)
    holder.binding.lifecycleOwner = fragment
  }
}