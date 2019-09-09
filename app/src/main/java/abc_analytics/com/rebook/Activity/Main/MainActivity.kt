package abc_analytics.com.rebook.Activity.Main

import abc_analytics.com.rebook.*
import abc_analytics.com.rebook.Activity.Capture.CaptureActivity
import abc_analytics.com.rebook.Activity.Login.LoginActivity
import abc_analytics.com.rebook.Activity.ScrapList.ScrapListActivity
import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var mBookAdapter: BookListAdapter
    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkPerm()
        Log.d("hello ", "aaa")
        fab.setOnClickListener { view ->
            startActivity(Intent(this, CaptureActivity::class.java))
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        //externalMediaDirs.map{Log.d(TAG, "externalMediaDirs: ${it.absolutePath}")}
    }

    override fun onResume() {
        super.onResume()
        //val bookArray = dataBook()
        try {
            launch {
                val bookArray = dataBookFromFB()
                updateUI(bookArray.toTypedArray())
            }
        } catch (e: Throwable) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            e.cause
        }
    }

    //Main scope
    private suspend fun dataBookFromFB(): List<Book?> {
        val user = FirebaseAuth.getInstance().currentUser ?: return listOf<Book>()
        val ref = db.collection("users").document(user.uid)
        val tasks = withContext(Dispatchers.IO) {
            listOf(
                ref.collection("books").get(),
                ref.collection("scraps").get()
            )
        }
        val books = tasks.get(0).await().documents.map { it.toObject(Book::class.java) }
        var scraps = tasks.get(1).await().documents.map { it.toObject(Scrap::class.java) }
        val mapScraps = scraps.groupingBy { it?.isbn }.eachCount()
        return books.map {
            if (mapScraps.containsKey(it?.isbn)) {
                it?.numScraps = mapScraps[it?.isbn] ?: 0
            } else {
                it?.numScraps = 0
            }
            it
        }.sortedBy { it?.updated_at }.reversed()
    }

    private fun updateUI(bookArray: Array<Book?>) {
        title = mAuth.currentUser?.displayName ?: "no yet login"
        mBookAdapter =
            BookListAdapter(this@MainActivity,
                bookArray.toMutableList(),
                onItemClicked = { book ->
                    //Log.d("hello adapter click", book?.title ?: "no book")
                    //Toast.makeText(this, book?.title ?: "no book", Toast.LENGTH_LONG).show()
                    val sendIntent = Intent(this@MainActivity, ScrapListActivity::class.java)
                    sendIntent.putExtra(FROM_ACTIVITY, this.localClassName)
                    sendIntent.putExtra(EXTRA_BOOK, book)
                    startActivity(sendIntent)
                },
                onItemLongClicked = { book ->
                    Log.d("hello adapter long click", book?.title ?: "no book")
                    Toast.makeText(this, book?.title ?: "no book", Toast.LENGTH_LONG).show()
                })
        recyclerViewBook.adapter = mBookAdapter
        recyclerViewBook.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun checkPerm() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getContentsInfo(this)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) return
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                getContentsInfo(this)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}