package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_book.view.*

class ScrapListAdapter(
    context: Context, var scraps: MutableList<Scrap>, val onItemClicked: (Scrap?) -> Unit
) : RecyclerView.Adapter<ScrapListAdapter.ScrapViewHolder>() {


    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    class ScrapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doc: TextView = TODO()//itemView.textViewBookTitle
        fun updateWithUrl(url: String?) {
            url?.let { Picasso.get().load(it).into(itemView.imageViewThumbNail) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ScrapViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}