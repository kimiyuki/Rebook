package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_scrap_list.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class ScrapListActivity : AppCompatActivity(), CoroutineScope {
    lateinit var mScrapAdapter: ScrapListAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var book: Book
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap_list)

        book = intent.getParcelableExtra<Book>(EXTRA_BOOK)
        title = book.title
        fabScrapList.setOnClickListener { view ->
            val sendIntent = Intent(this@ScrapListActivity, CaptureActivity::class.java)
            sendIntent.putExtra(EXTRA_BOOK, book)
            startActivityForResult(sendIntent, SCRAPLIST_CAPTURE_REQUEST_CODE)
        }

        launch {
            val scrapArray = dataScrapFromFB(book.isbn)
            updateUI(scrapArray)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("BOOK", book.copy())
    }

    fun updateUI(scrapArray: List<Scrap?>) {
        mScrapAdapter = ScrapListAdapter(
            this@ScrapListActivity, scrapArray, user!!.uid, book.isbn,
            onItemClicked = { scrap ->
                //Toast.makeText(this, scrap?.doc, Toast.LENGTH_LONG).show()
                val sendIntent = Intent(this@ScrapListActivity, ScrapDetailActivity::class.java)
                sendIntent.putExtra(DOC_CONTENT, scrap.doc)
                sendIntent.putExtra(IMG_URI, scrap.imagePath)
                sendIntent.putExtra(ISBN_CONTENT, book.isbn)
                sendIntent.putExtra(TITLE_CONTENT, book.title)
                sendIntent.putExtra(FROM_ACTIVITY, this.localClassName)
                sendIntent.putExtra(SCRAP_ID, scrap.id)
                sendIntent.putExtra(SCRAP_PAGENUMBER, scrap.pageNumber)
                startActivityForResult(sendIntent, SCRAPLIST_DETAIL_INTENT)
            },
            deleteScrap = { scrap ->
                launch {
                    deleteScrap(scrap)
                    recyclerViewScrap.adapter?.notifyDataSetChanged()
                }
            }
        )
        recyclerViewScrap.adapter = mScrapAdapter
        recyclerViewScrap.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private suspend fun deleteScrap(scrap: Scrap) {
        withContext(Dispatchers.IO) {
            db.collection("users").document(user!!.uid)
                .collection("scraps").document(scrap.id).delete().await()
            Log.d(TAG, "scrap ${scrap.id} deleted")
        }
    }

    private suspend fun dataScrapFromFB(isbn: String): List<Scrap?> {
        val scraps = withContext(Dispatchers.Default) {
            val ref = db.collection("users")
                .document(user!!.uid).collection("scraps").whereEqualTo("isbn", isbn)
            ref.get().await().documents.map {
                val o = it.toObject(Scrap::class.java)
                o?.id = it.id; o
            }.filter { it?.isbn == isbn }
        }
        Log.d(TAG, "size ${scraps.size}")
        Log.d(TAG, "scraps ${scraps}")
        return scraps
    }

    private suspend fun dataBookFromFB(isbn: String): Book {
        return withContext(Dispatchers.IO) {
            val s = db.collection("users").document(user!!.uid)
                .collection("books").whereEqualTo("isbn", isbn).get().await()
            if (s.size() > 0) {
                s.documents.first().toObject(Book::class.java) as Book
            } else {
                Book()
            }
        }
    }
}
