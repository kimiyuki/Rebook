package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_book.view.*

class BookListAdapter(
    context: Context, var books: MutableList<Book>,
    val onItemClicked: (Book?) -> Unit
) :
    RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var book_title = itemView.textViewBookTitle
        fun updateWithUrl(url: String?) {
            if (url != null) Picasso.get().load(url).into(itemView.imageViewThumbNail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_book, parent, false)

        val holder = BookViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            val book = books[position]
            onItemClicked(book)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        Log.d("hello BindBiewHolder", books[position].title.toString())
        holder.updateWithUrl(books[position].thumbnailUrl)
        holder.book_title.text = books[position].title
    }
}