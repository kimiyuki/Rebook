package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class FireStoreRep {
  val TAG = "FIREBASE_REPOSITORY"

  // get saved addresses from firebase
  companion object {
    suspend fun getBooks(user: FirebaseUser): List<Book?> {
      //throw UnsupportedOperationException()
      val ref = FirebaseFirestore.getInstance()
        .collection("users").document(user.uid)
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

    suspend fun deleteBook(user: FirebaseUser, book: Book) {
      var ret = FirebaseFirestore.getInstance()
        .collection("users").document(user.uid)
        .collection("books").whereEqualTo("isbn", book.isbn)
        .get().await().documents.get(0)?.reference
      ret?.delete()
    }

    suspend fun uploadBook(user: FirebaseUser, book: Book) {
      //upload
      var ret = FirebaseFirestore.getInstance()
        .collection("users").document(user.uid)
        .collection("books").whereEqualTo("isbn", book.isbn)
        .get().await()
      if (ret.size() > 0) return

      val data = mutableMapOf(
        "isbn" to book.isbn, "user" to user.uid,
        "localfile_path" to book.lastImagePath,
        "title" to book.title, "thumbnailUrl" to book.thumbnailUrl, "authors" to book.authors,
        "created_at" to Date(), "updated_at" to Date(), "numScraps" to 0
      )
      FirebaseFirestore.getInstance().collection("users").document(user.uid)
        .collection("books").add(data).await()
    }
  }
}
