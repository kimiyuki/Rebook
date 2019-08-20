package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_scrap.view.*

class ScrapListAdapter(
    context: Context, var scraps: List<Scrap?>, val onItemClicked: (Scrap?) -> Unit
) : RecyclerView.Adapter<ScrapListAdapter.ScrapListViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        scraps = scraps.sortedBy { it?.page }
    }

    class ScrapListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doc: TextView = itemView.textViewScrapDoc
        val n: TextView = itemView.textViewScrapPage
        val storage = FirebaseStorage.getInstance().reference
        fun updateWithUrl(path: String?) {
            if (path == null && path == "") return
            val newPath = path?.replace("""([^/]+\.png)""".toRegex(), "thumb_$1")
            storage.child(newPath!!).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(itemView.imageViewScrapImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapListViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_scrap, parent, false)
        view.layoutParams.height = parent.measuredHeight / 4
        val holder = ScrapListViewHolder(view)
        view.setOnClickListener { scraps[holder.adapterPosition].also { onItemClicked(it) } }
        return holder
    }

    override fun getItemCount() = scraps.size

    override fun onBindViewHolder(holder: ScrapListViewHolder, position: Int) {
        if (scraps[position] == null) return
        holder.apply {
            updateWithUrl(scraps[position]?.imagePath)
            n.text = scraps[position]?.page.toString()
            doc.text = scraps[position]?.doc
        }
    }
}