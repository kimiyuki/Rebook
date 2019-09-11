package abc_analytics.com.rebook.Activity.ScrapList

import abc_analytics.com.rebook.*
import abc_analytics.com.rebook.Activity.Capture.CaptureActivity
import abc_analytics.com.rebook.Activity.Main.MyViewModel
import abc_analytics.com.rebook.Activity.ScrapDetail.ScrapDetailActivity
import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.Repository.FireStoreRep.deleteScrap
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_scrap_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ScrapListActivity : AppCompatActivity(), CoroutineScope {
  lateinit var mScrapAdapter: ScrapListAdapter
  private val db = FirebaseFirestore.getInstance()
  private lateinit var book: Book
  private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
  private val job = Job()
  private lateinit var viewModel: MyViewModel

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scrap_list)
    book = intent.getParcelableExtra<Book>(EXTRA_BOOK).also { title = it.title }
    fabScrapList.setOnClickListener { view ->
      Intent(this@ScrapListActivity, CaptureActivity::class.java).apply {
        putExtra(EXTRA_BOOK, book)
        startActivityForResult(this, SCRAPLIST_CAPTURE_REQUEST_CODE)
      }
    }
    if (user == null) return
    ViewModelProvider(this).get(MyViewModel::class.java)
      .getScraps(user, book.isbn).observe(this, Observer {
        updateUI(it)
      })
  }

  fun updateUI(scrapArray: List<Scrap>): Unit {
    if (user == null) return
    mScrapAdapter = ScrapListAdapter(
      this@ScrapListActivity, scrapArray, user.uid, book.isbn,
      onItemClicked = moveToScrapDetqail(),
      deleteScrap = _deleteScrap(user, scrapArray)
    )
    recyclerViewScrap.adapter = mScrapAdapter
    recyclerViewScrap.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
  }

  private fun _deleteScrap(user: FirebaseUser, scrapArray: List<Scrap>): (Scrap, Int) -> Unit {
    return { scrap, index ->
      launch { deleteScrap(user, scrap) }
      mScrapAdapter.scraps.removeAt(index)
      mScrapAdapter.notifyItemRemoved(index)
      mScrapAdapter.notifyItemRangeChanged(index, scrapArray.size)
      Toast.makeText(this@ScrapListActivity, "delete scrap", Toast.LENGTH_LONG).show()
    }
  }

  private fun moveToScrapDetqail(): (Scrap) -> Unit {
    return { scrap ->
      Intent(this@ScrapListActivity, ScrapDetailActivity::class.java).let {
        it.putExtra(EXTRA_SCRAP, scrap)
        it.putExtra(FROM_ACTIVITY, this.localClassName)
        startActivityForResult(it, SCRAPLIST_DETAIL_INTENT)
      }
    }
  }
}
