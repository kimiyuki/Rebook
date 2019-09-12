package abc_analytics.com.rebook.Model

import com.google.firebase.auth.FirebaseAuth
import java.io.Serializable
import java.util.*

data class Scrap(
  val doc: String = "", val created_at: Date = Date(), val updated_at: Date = Date(),
  var imagePath: String = "", val localImagePath: String = "", val pageNumber: Int = 0,
  val isbn: String = "", val bookTitle: String = "", var firestoreId: String = ""
) : Serializable {
  val id: String by lazy { "${FirebaseAuth.getInstance().currentUser!!.uid}_${System.currentTimeMillis()}" }
}

