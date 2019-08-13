package abc_analytics.com.rebook

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_doc.*

class DocActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc)
        backButton.setOnClickListener { v ->
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val text = intent.getStringExtra(DOC_CONTENT)
        Log.d("hello text", text?.toString() ?: "no text")
        if (text != null) docTextView.text = text
    }
}
