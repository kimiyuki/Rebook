package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_book.view.*

class BookListAdapter(
    context: Context, var books: MutableList<Book>, val onItemClicked: (Book?) -> Unit
) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.textViewBookTitle
        fun updateWithUrl(url: String?) {
            url?.let { Picasso.get().load(it).into(itemView.imageViewThumbNail) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_book, parent, false)
        val holder = BookViewHolder(view)
        view.setOnClickListener { books[holder.adapterPosition].also { onItemClicked(it) } }
        return holder
    }

    override fun getItemCount() = books.size
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        Log.d("hello BindBiewHolder", books[position].title.toString())
        holder.apply {
            updateWithUrl(books[position].thumbnailUrl)
            bookTitle.text = books[position].title
        }
    }
}