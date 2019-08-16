package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
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

class MainActivity : AppCompatActivity() {
    private lateinit var mBookAdapter: BookListAdapter
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.d("hello ", "aaa")
        fab.setOnClickListener { view ->
            startActivity(Intent(this, CaptureActivity::class.java))
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        dataBook()
        checkPerm()
        testFireStore()
        Log.d("hello", mAuth.currentUser?.displayName ?: "no login")
        title = mAuth.currentUser?.displayName ?: "no yet login"
    }

    private fun testFireStore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("scraps")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun checkPerm() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getContentsInfo(this)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
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

    private fun dataBook() {
        val thumbs = arrayOf(
            "http://books.google.com/books/content?id=m1OjDwAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api"
            ,
            "http://books.google.com/books/content?id=n_iFPgAACAAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api"
            ,
            "http://books.google.com/books/content?id=W-oGBAAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api"
        )
        val BookArray = mutableListOf<Book>(
            Book(id = "1", isbn = "aaa", title = "hello book1", scraps = arrayListOf(), thumbnailUrl = thumbs[0]),
            Book(id = "2", isbn = "bbb", title = "hello book2", scraps = arrayListOf(), thumbnailUrl = thumbs[1]),
            Book(id = "3", isbn = "ccc", title = "hello book3", scraps = arrayListOf(), thumbnailUrl = thumbs[2])
        )
        mBookAdapter = BookListAdapter(this@MainActivity, BookArray,
            onItemClicked = { book ->
                Log.d("hello adapter click", book?.title ?: "no book")
                Toast.makeText(this, book?.title ?: "no book", Toast.LENGTH_LONG).show()
            })
        recyclerViewBook.adapter = mBookAdapter
        recyclerViewBook.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

}
