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
    context: Context, var books: MutableList<Book?>,
    val onItemClicked: (Book?) -> Unit,
    val onItemLongClicked: (Book?) -> Unit
) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        Picasso.Builder(context).apply {
            loggingEnabled(true)
            indicatorsEnabled(true)
        }
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.textViewBookTitle
        val numScraps: TextView = itemView.textViewNumScraps
        fun updateWithUrl(url: String?) {
            //if (url == null || url.isEmpty()) return
            //TODO() 機種によって、thumnailが描画されない. 原因追求の方法がわからない
            Picasso.get().load(url?.toUri()).into(itemView.imageViewThumbNail)
            Log.d(TAG, "url ${url}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_book, parent, false)
        view.layoutParams.height = parent.measuredHeight / 10
        val holder = BookViewHolder(view)
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
            updateWithUrl(books[position]?.thumbnailUrl)
            numScraps.text = "${books[position]?.numScraps.toString()} scraps"
            bookTitle.text = books[position]?.title
        }
    }
}