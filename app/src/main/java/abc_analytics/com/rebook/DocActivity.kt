package abc_analytics.com.rebook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_doc.*
import java.io.ByteArrayOutputStream


class DocActivity : AppCompatActivity() {
    lateinit var btm: Bitmap
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
        val db = FirebaseFirestore.getInstance()
        val doc = textViewDoc.text.toString()
        val title = textViewTitleDoc.text.toString()
        btm = imageViewScrapCaptured.drawable.toBitmap()
        Log.d("hello", title)
        val byteArrayOutputStream = ByteArrayOutputStream()
        btm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bitmapString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        val data = hashMapOf(
            "user" to user.hashCode(),
            "title" to title, "doc" to doc, "image" to bitmapString
        )
        db.collection("scraps").add(data)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it.id}")
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val text = intent.getStringExtra(DOC_CONTENT)
        val fpath = intent.getStringExtra(IMG_URI)
        val title = intent.getStringExtra(TITLE_CONTENT)
        Log.d("hello text", text?.toString() ?: "no text")
        if (text != null) textViewDoc.text = text
        if (fpath != null) {
            btm = BitmapFactory.decodeFile(fpath)
            imageViewScrapCaptured.setImageBitmap(btm)
        }
        textViewTitleDoc.text = title
    }
}
