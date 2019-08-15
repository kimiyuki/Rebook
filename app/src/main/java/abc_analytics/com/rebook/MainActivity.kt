package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mBookAdapter: BookListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, CaptureActivity::class.java))
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        dataBook()
    }

    private fun dataBook() {
        val thumbs = arrayOf(
            "http://books.google.com/books/content?id=m1OjDwAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api",
            "http://books.google.com/books/content?id=W-oGBAAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api",
            "http://books.google.com/books/content?id=n_iFPgAACAAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api"
        )
        val BookArray = mutableListOf<Book>(
            Book(id = "1", isbn = "aaa", title = "hello book1", scraps = arrayListOf(), thumbnailUrl = thumbs[0]),
            Book(id = "2", isbn = "bbb", title = "hello book2", scraps = arrayListOf(), thumbnailUrl = thumbs[1]),
            Book(id = "3", isbn = "ccc", title = "hello book3", scraps = arrayListOf(), thumbnailUrl = thumbs[2])
        )
        mBookAdapter = BookListAdapter(this@MainActivity, BookArray,
            onItemClicked = { book ->
                //ISSUE endless execution? update invoke another selection item?
                Log.d("hello adapter click", book?.title ?: "no book")
                Toast.makeText(this, book?.title ?: "no book", Toast.LENGTH_LONG).show()
            })
        recyclerViewBook.adapter = mBookAdapter
        recyclerViewBook.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
    }

}
