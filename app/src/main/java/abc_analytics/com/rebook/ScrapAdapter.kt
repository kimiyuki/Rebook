package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ScrapAdapter(
    context: Context, var scraps: List<Scrap?>, val onItemClicked: (View) -> Unit
) : RecyclerView.Adapter<ScrapAdapter.ScrapViewHolder>() {

    class ScrapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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
