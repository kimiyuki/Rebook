package abc_analytics.com.rebook.Activity.Main

import abc_analytics.com.rebook.*
import abc_analytics.com.rebook.Activity.Capture.CaptureActivity
import abc_analytics.com.rebook.Activity.Login.LoginActivity
import abc_analytics.com.rebook.Activity.ScrapList.ScrapListActivity
import abc_analytics.com.rebook.Model.Book
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
  private lateinit var mBookAdapter: BookListAdapter
  private val mAuth = FirebaseAuth.getInstance()
  private lateinit var myViewModel: MyViewModel
  private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
  private val job = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    Timber.plant(Timber.DebugTree())
    checkPerm()
    val user = FirebaseAuth.getInstance().currentUser ?: return
    Timber.i(user.displayName)
    Timber.plant(Timber.DebugTree())
    myViewModel = ViewModelProvider(this@MainActivity).get(MyViewModel::class.java)
    launch(coroExHandler) {
      myViewModel.getBooks(user).observe(this@MainActivity, Observer {
        updateUI(it.toTypedArray())
      })
    }
    fab.setOnClickListener { view ->
      startActivity(Intent(this, CaptureActivity::class.java))
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }
  }

  private fun updateUI(bookArray: Array<Book?>) {
    title = mAuth.currentUser?.displayName ?: "no yet login"
    mBookAdapter = BookListAdapter(
      this@MainActivity,
      bookArray.toMutableList(),
      onItemClicked = moveToScrapList(),
      onItemLongClicked = deleteBook(user)
    )
    recyclerViewBook.adapter = mBookAdapter
    recyclerViewBook.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
  }


  private fun moveToScrapList(): (Book?) -> Unit {
    return { book ->
      val sendIntent = Intent(this@MainActivity, ScrapListActivity::class.java)
      sendIntent.putExtra(WHICH_ACTIVITY, this.localClassName)
      sendIntent.putExtra(EXTRA_BOOK, book)
      startActivity(sendIntent)
    }
  }

  private fun deleteBook(user: FirebaseUser?): (Book?) -> Unit {
    return { book ->
      if (book != null && user != null) {
        AlertDialog.Builder(this@MainActivity)
          .setTitle("削除")
          .setMessage("削除します")
          .setPositiveButton("OK") { dialog, which ->
            launch {
              myViewModel.deleteBook(user, book)
              Toast.makeText(
                this@MainActivity, book.title, Toast.LENGTH_LONG
              ).show()
            }
          }.show()
      }
    }
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

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
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
