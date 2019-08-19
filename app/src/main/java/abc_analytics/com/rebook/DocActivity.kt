package abc_analytics.com.rebook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_doc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

class DocActivity : AppCompatActivity() {
    lateinit var btm: Bitmap
    var isbn: String = ""
    var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc)
        backButton.setOnClickListener { v ->
            startActivity(Intent(this, MainActivity::class.java))
        }
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
        val user_hash = user.hashCode()
        btm = imageViewScrapCaptured.drawable.toBitmap()

        val storageRef = FirebaseStorage.getInstance().reference.child("images")
        val fileRef = storageRef.child("${user_hash}i_${System.currentTimeMillis()}.png")
        val db = FirebaseFirestore.getInstance()
        val byteArrayOutputStream = ByteArrayOutputStream()
        btm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()

        //upload image file
        GlobalScope.launch(Dispatchers.Default) {
            val task = fileRef.putBytes(byteArrayOutputStream.toByteArray()).await()
            Looper.prepare()
            Toast.makeText(
                this@DocActivity, "upload Image ${task.bytesTransferred / 1000000}M",
                Toast.LENGTH_LONG
            ).show()
            Looper.loop()
        }

        //upload scrap
        val data = hashMapOf(
            "isbn" to isbn,
            "user" to user_hash, "title" to title, "doc" to doc, "imagePath" to fileRef.path,
            "created_at" to Date(), "upcated_at" to Date()
        )
        db.collection("scraps").add(data)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it.id}")
                Toast.makeText(this, "upload succeed", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "upload failed", Toast.LENGTH_LONG).show()
                Log.w(TAG, "Error adding document", it)
            }
        //update book
        db.collection("books").whereEqualTo("isbn", isbn).get().addOnSuccessListener {
            val n = it.documents.first().get("numScraps", Int::class.java) ?: 0
            it.documents.first().reference.update(mapOf("updated_at" to Date(), "numScraps" to (n + 1)))
            Toast.makeText(this, "update succeed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val text = intent.getStringExtra(DOC_CONTENT)
        val fpath = intent.getStringExtra(IMG_URI)
        val title = intent.getStringExtra(TITLE_CONTENT)
        isbn = intent.getStringExtra(ISBN_CONTENT)
        Log.d("hello text", text?.toString() ?: "no text")
        if (text != null) textViewDoc.text = text
        if (fpath != null) {
            btm = BitmapFactory.decodeFile(fpath)
            imageViewScrapCaptured.setImageBitmap(btm)
        }
        textViewTitleDoc.text = title
    }
}
