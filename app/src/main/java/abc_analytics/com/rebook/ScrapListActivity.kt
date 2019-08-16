package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_scrap_list.*

class ScrapListActivity : AppCompatActivity() {
    lateinit var mScrapAdapter: ScrapListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap_list)
        getContentsInfo(this)
        //updateUI()
    }

    fun updateUI() {
        val scraps = mutableListOf<Scrap>()
        TODO("create scraps list for check")
        mScrapAdapter = ScrapListAdapter(this@ScrapListActivity, scraps,
            onItemClicked = { scrap ->
                Log.d("hello adapter scrapb click", "aaa")
                Toast.makeText(this, scrap?.doc, Toast.LENGTH_LONG).show()
            })
        recyclerViewScrap.adapter = mScrapAdapter
        recyclerViewScrap.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    fun dataScraps(): MutableList<Scrap> {
        TODO()
        //val img
        //val d = ContextCompat.getDrawable(this, R.drawable.abc_btn_colored_material)
//       val s = Scrap(doc="hello world", created_at=LocalDateTime.now(), image=, book_id = "abc")
    }
}
