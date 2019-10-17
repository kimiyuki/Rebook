package com.abc_analytics.rebook.Utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.abc_analytics.rebook.data.Book
import com.abc_analytics.rebook.ui.home.BookListAdapter

//class ResultDiffCallback(val oldItems:List<Book>, val newItems:List<Book>): DiffUtil.Callback(){
//  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
//    oldItems[oldItemPosition].hashCode() == newItems[newItemPosition].hashCode()
//  override fun getOldListSize() = oldItems.size
//  override fun getNewListSize() = newItems.size
//  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
//    oldItems[oldItemPosition] == newItems[newItemPosition]
//}
//
//@BindingAdapter("android:viewmodels")
//fun RecyclerView.setViewModels(books: List<Book>) {
//  val adapter = adapter as BookListAdapter
//  var diff = DiffUtil.calculateDiff(
//    ResultDiffCallback(adapter.books, books),true)
//  adapter.books = books
//  diff.dispatchUpdatesTo(adapter)
//}
