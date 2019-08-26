package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_scrap.view.*
import java.text.SimpleDateFormat

class ScrapListAdapter(
    context: Context, var scraps: List<Scrap?>, val uid: String, val isbn: String,
    val onItemClicked: (Scrap?) -> Unit
) : RecyclerView.Adapter<ScrapListAdapter.ScrapListViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        scraps = scraps.sortedBy { it?.pageNumber }
        Picasso.Builder(context).run {
            loggingEnabled(true)
            indicatorsEnabled(true)
        }
    }

    class ScrapListViewHolder(itemView: View, uid: String, isbn: String) :
        RecyclerView.ViewHolder(itemView) {
        val doc: TextView = itemView.textViewScrapDoc
        val n: TextView = itemView.textViewScrapPage
        val storage = FirebaseStorage.getInstance().reference
        val update_time = itemView.textViewUpdateTime

        fun updateWithUrl(path: String?) {
            if (path == null && path == "") return
            val newPath = path?.replace("""([^/]+\.png)""".toRegex(), "thumb_$1")
            storage.child(newPath!!).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it)
                    .fit().centerInside()
                    .rotate(90f)
                    .into(itemView.imageViewScrapImage)
                Log.d(TAG, "download for picasso $it")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapListViewHolder {
        val view = mLayoutInflater.inflate(R.layout.row_scrap, parent, false)
        view.layoutParams.height = parent.measuredHeight / 2
        val holder = ScrapListViewHolder(view, uid, isbn)
        view.setOnClickListener { scraps[holder.adapterPosition].also { onItemClicked(it) } }
        return holder
    }

    override fun getItemCount() = scraps.size

    override fun onBindViewHolder(holder: ScrapListViewHolder, position: Int) {
        if (scraps[position] == null) return
        holder.apply {
            updateWithUrl(scraps[position]?.imagePath)
            n.text = scraps[position]?.pageNumber.toString()
            doc.text = scraps[position]?.doc
            update_time.text = SimpleDateFormat("MM/dd HH:mm").format(scraps[position]?.updated_at)
        }
    }
}