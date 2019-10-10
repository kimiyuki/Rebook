package com.abc_analytics.rebook.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abc_analytics.rebook.MainViewModel
import com.abc_analytics.rebook.MyApp
import com.abc_analytics.rebook.R
import com.abc_analytics.rebook.data.Book
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.book_list.view.*

class BookListAdapter(val bookList:List<Book>, val viewModel:MainViewModel)
  :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  val appContext = MyApp.appContext
  inner class ViewHolder(override val containerView: View):
    RecyclerView.ViewHolder(containerView), LayoutContainer{
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.book_list, parent, false)
    return ViewHolder(view)
  }

  override fun getItemCount() = bookList.size

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    holder.apply{
      val book = bookList[position]
      itemView.textViewBookTitle.text = book.title
      itemView.textViewBookAuthor.text = book.author
      //itemView.txtThemeCard.text = appContext.resources.getString(themeData.themeNameResId)
//      Glide.with(appContext).load(themeData.themeSqPicResId).into(itemView.imgTheme)
//      itemView.imgTheme.setOnClickListener{
//        viewModel.setTheme(themeData)
//      }
    }
  }
}