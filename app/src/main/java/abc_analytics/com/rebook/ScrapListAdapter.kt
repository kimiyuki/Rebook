package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_scrap.view.*

class ScrapListAdapter(
    context: Context, var scraps: List<Scrap?>, val onItemClicked: (Scrap?) -> Unit
) : RecyclerView.Adapter<ScrapListAdapter.ScrapViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        scraps = scraps.sortedBy { it?.page }
    }

    class ScrapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doc: TextView = itemView.textViewScrapDoc
        val n: TextView = itemView.textViewScrapPage
        fun updateWithUrl(url: String?) {
            val _url =
                "http://books.google.com/books/content?id=n_iFPgAACAAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api"
            _url.let { Picasso.get().load(it).into(itemView.imageViewScrapImage) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_scrap, parent, false)
        val holder = ScrapListAdapter.ScrapViewHolder(view)
        view.setOnClickListener { scraps[holder.adapterPosition].also { onItemClicked(it) } }
        return holder
    }

    override fun getItemCount() = scraps.size

    override fun onBindViewHolder(holder: ScrapViewHolder, position: Int) {
        if (scraps[position] == null) return
        holder.apply {
            updateWithUrl(scraps[position]?.imageUrl)
            n.text = scraps[position]?.page.toString()
            doc.text = scraps[position]?.doc
        }
    }
}