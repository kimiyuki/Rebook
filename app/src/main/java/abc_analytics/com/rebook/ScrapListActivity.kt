package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Scrap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_scrap_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScrapListActivity : AppCompatActivity() {
    lateinit var mScrapAdapter: ScrapListAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap_list)

        val intent = intent
        val isbn: String = intent.getStringExtra(EXTRA_BOOK)
        GlobalScope.launch(Dispatchers.Main) {
            val scrapArray = dataScrapFromFB(isbn)
            updateUI(scrapArray)
        }
    }

    fun updateUI(scrapArray: List<Scrap?>) {
        mScrapAdapter = ScrapListAdapter(
            this@ScrapListActivity, scrapArray,
            onItemClicked = { scrap ->
                Log.d("hello adapter scrapb click", "aaa")
                Toast.makeText(this, scrap?.doc, Toast.LENGTH_LONG).show()
            })
        recyclerViewScrap.adapter = mScrapAdapter
        recyclerViewScrap.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private suspend fun dataScrapFromFB(isbn: String): List<Scrap?> {
        val snapshots = withContext(Dispatchers.Default) {
            val ref = db.collection("scraps")
            ref.get().await().documents.map { it.toObject(Scrap::class.java) }.filter { it?.isbn == isbn }
        }
        Log.d(TAG, "isbn ${isbn}")
        Log.d(TAG, "size ${snapshots.size}")
        return snapshots
    }
}
