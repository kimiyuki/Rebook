package abc_analytics.com.rebook.Activity.ScrapList

import abc_analytics.com.rebook.Activity.Login.TAG
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_scrap.view.*
import java.text.SimpleDateFormat

class ScrapListAdapter(
    val context: Context, var scraps: MutableList<Scrap?>, val uid: String, val isbn: String,
    val onItemClicked: (Scrap) -> Unit,
    val deleteScrap: (Scrap, Int) -> Unit
) : RecyclerView.Adapter<ScrapListAdapter.ScrapListViewHolder>() {

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        scraps.sortBy { it?.pageNumber }
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
        val holder =
            ScrapListViewHolder(
                view,
                uid,
                isbn
            )
        //holder.adaption is not yet real index but an UI event goes on it's ok. maybe.
        //https://developer.android.com/reference/android/support/v7/widget/RecyclerView.ViewHolder#getadapterposition
        view.setOnClickListener { onItemClicked(scraps[holder.adapterPosition]!!) }
        view.setOnLongClickListener { v ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("削除")
            builder.setMessage("削除しますか")
            builder.setPositiveButton("OK") { _, _ ->
                deleteScrap(scraps[holder.adapterPosition]!!, holder.adapterPosition)
            }
            builder.setNegativeButton("CANCEL", null)
            val dialog = builder.create()
            dialog.show()
            Log.d("hello", "dry run: delete the scrap")
            true
        }
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