package abc_analytics.com.rebook

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_scrap_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ScrapDetailActivity : AppCompatActivity() {
    var isbn: String = ""
    var localImageFilePath: String = ""
    var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var pageNumber: Int = 0
    val storageRef = FirebaseStorage.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap_detail)
        okButton.setOnClickListener { v -> uploadData(v) }
    }


    fun uploadData(v: View) {
        if (isbn == "") {
            Toast.makeText(this, "no isbn", Toast.LENGTH_LONG).show()
            return
        }
        //data setup
        val doc = textViewDoc.text.toString()
        val title = textViewTitleDoc.text.toString()
        val userString = user!!.uid

        val storageRef = FirebaseStorage.getInstance().reference
            .child("images")
            .child(userString)
            .child(isbn)
        val fileRef = storageRef.child("${System.currentTimeMillis()}.jpg")
        val db = FirebaseFirestore.getInstance()
        val byteArrayOutputStream = ByteArrayOutputStream()

        //upload image file
        GlobalScope.launch(Dispatchers.Default) {
            //val task = fileRef.putBytes(byteArrayOutputStream.toByteArray()).await()
            val task = fileRef.putFile(Uri.fromFile(File(localImageFilePath))).await()
            Looper.prepare()
            Toast.makeText(
                this@ScrapDetailActivity, "upload Image ${task.bytesTransferred / 1000000}M",
                Toast.LENGTH_LONG
            ).show()
            Looper.loop()
        }

        //upload scrap
        val data = hashMapOf(
            "isbn" to isbn,
            "user" to userString, "title" to title, "doc" to doc,
            "imagePath" to fileRef.path, "localStoragePath" to localImageFilePath,
            "created_at" to Date(), "upcated_at" to Date()
        )
        val docRef = db.collection("users").document(user!!.uid)
        docRef.collection("scraps").add(data)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it.id}")
                Toast.makeText(this, "upload succeed", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "upload failed", Toast.LENGTH_LONG).show()
                Log.w(TAG, "Error adding document", it)
            }
        //update book
        docRef.collection("books").whereEqualTo("isbn", isbn).get().addOnSuccessListener {
            val n = it.documents.first().get("numScraps", Int::class.java) ?: 0
            it.documents.first().reference.update(mapOf("updated_at" to Date(), "numScraps" to (n + 1)))
            Toast.makeText(this, "update succeed", Toast.LENGTH_LONG).show()
        }
    }

    fun downLoadFile(fpath: String) {
        //val prop = ImageView.ROTATION
        val islandRef = storageRef.child(fpath)
        val localFile = File.createTempFile("images", "jpg")
        islandRef.getFile(localFile).addOnSuccessListener {
            //            val ei = ExifInterface(localFile.absolutePath)
//            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            imageViewScrapCaptured.setImageURI(localFile.toUri())
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val text = intent.getStringExtra(DOC_CONTENT)
        localImageFilePath = intent.getStringExtra(IMG_URI)
        title = intent.getStringExtra(TITLE_CONTENT)
        isbn = intent.getStringExtra(ISBN_CONTENT)
        pageNumber = intent.getIntExtra(PAGENUMBER_CONTENT, 0)
        val fromActivity = intent.getStringExtra(FROM_ACTIVITY)
        Log.d(TAG, "localImageFilePath:${localImageFilePath}")
        textViewTitleDoc.text = title
        textViewDoc.text = text.replace("(\n)".toRegex(), "").replace(" ".toRegex(), "\n")
        textViewPageNumber.text = "page:${pageNumber}"
        Log.d(TAG, "fromActivity:${fromActivity}")
        if (fromActivity == "ScrapListActivity") {
            okButton.isVisible = false
            downLoadFile(localImageFilePath)
        } else if (fromActivity == "CaptureActivity") {
            okButton.isVisible = true
            imageViewScrapCaptured.setImageURI(localImageFilePath.toUri())
        }
        if (text != null) textViewDoc.text = text
    }
}
