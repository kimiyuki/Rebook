package abc_analytics.com.rebook.Activity.ScrapDetail

import abc_analytics.com.rebook.EXTRA_SCRAP
import abc_analytics.com.rebook.FROM_ACTIVITY
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.R
import abc_analytics.com.rebook.Repository.CloudStorageRep.downLoadFile
import abc_analytics.com.rebook.Repository.CloudStorageRep.uploadFile
import abc_analytics.com.rebook.Repository.FireStoreRep.updatePageNumberInScrap
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_scrap_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext


class ScrapDetailActivity : AppCompatActivity(), CoroutineScope {
  private val db = FirebaseFirestore.getInstance()
  private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
  private val scrap: Scrap by lazy { intent.getSerializableExtra(EXTRA_SCRAP) as Scrap }
  private val job = Job()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scrap_detail)
    textViewDoc.text = scrap.doc.replace("(\n)".toRegex(), "").replace(" ".toRegex(), "\n")
    Timber.d("pageNumber:${scrap.pageNumber}")
    editTextPageNumber.setText(scrap.pageNumber.toString())
    when (intent.getStringExtra(FROM_ACTIVITY)) {
      "Activity.ScrapList.ScrapListActivity" -> {
        okButton.isVisible = false
        launch {
          val file = downLoadFile(scrap.imagePath)
          imageViewScrapCaptured.setImageURI(file.toUri())
        }
      }
      "Activity.Capture.CaptureActivity" -> {
        okButton.isVisible = true
        imageViewScrapCaptured.setImageURI(scrap.localImagePath.toUri())
      }
    }
    if (user == null) return
    okButton.setOnClickListener { _ ->
      insertScrap(user!!, scrap)
      updateNumScrapsInBooks(user!!, scrap.isbn)
      launch { uploadFile(scrap.imagePath, scrap) }
    }
    editTextPageNumber.setOnFocusChangeListener { v, hasFocus ->
      if (!hasFocus) updatePageNumberInScrap(user!!, scrap, (v as EditText).text.toString().toInt())
    }
  }

  fun insertScrap(user: FirebaseUser, scrap: Scrap) {
    if (scrap.isbn == "") {
      Toast.makeText(this, "no isbn", Toast.LENGTH_LONG).show()
      return
    }
    db.collection("users").document(user.uid)
      .collection("scraps").add(scrap.copy(created_at = Date(), updated_at = Date()))
  }

  fun updateNumScrapsInBooks(user: FirebaseUser, isbn: String) {
    db.collection("users").document(user.uid)
      .collection("books").whereEqualTo("isbn", scrap.isbn)
      .get().addOnSuccessListener {
        val n = it.documents.first().get("numScraps", Int::class.java) ?: 0
        it.documents.first().reference.update(mapOf("updated_at" to Date(), "numScraps" to (n + 1)))
        //Toast.makeText(this, "update succeed", Toast.LENGTH_LONG).show()
      }
  }


}
