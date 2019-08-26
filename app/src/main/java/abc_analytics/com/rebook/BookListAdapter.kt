package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_book.view.*

class BookListAdapter(
    val context: Context, var books: MutableList<Book?>,
    val onItemClicked: (Book?) -> Unit,
    val onItemLongClicked: (Book?) -> Unit
) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)


    class BookViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.textViewBookTitle
        val numScraps: TextView = itemView.textViewNumScraps

        init {
            Picasso.Builder(context).run {
                loggingEnabled(true)
                indicatorsEnabled(true)
            }
        }
        fun updateWithUrl(url: String?, context: Context) {
            Log.d(TAG, "000 ${url}")
            if (url == null || url.isEmpty()) return
            Picasso.get().load(url.replace("http:", "https:").toUri())
                .into(itemView.imageViewThumbNail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_book, parent, false)
        view.layoutParams.height = parent.measuredHeight / 10
        val holder = BookViewHolder(view, context)
        view.setOnClickListener { books[holder.adapterPosition].also { onItemClicked(it) } }
        view.setOnLongClickListener { v ->
            books[holder.adapterPosition].also { onItemLongClicked(it) }
            return@setOnLongClickListener true
        }
        return holder
    }

    override fun getItemCount() = books.size
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        Log.d("hello BindBiewHolder", books[position]?.title.toString())
        if (books[position] == null) return
        holder.apply {
            updateWithUrl(books[position]?.thumbnailUrl, context)
            numScraps.text = "${books[position]?.numScraps.toString()} scraps"
            bookTitle.text = books[position]?.title
        }
    }
}