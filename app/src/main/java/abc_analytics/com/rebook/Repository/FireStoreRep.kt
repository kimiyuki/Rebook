package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.coroExHandler
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

object FireStoreRep {
  val TAG = "FIREBASE_REPOSITORY"
  // get saved addresses from firebase
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

  suspend fun deleteBook(user: FirebaseUser, book: Book): Boolean? {
    val t = withContext(coroExHandler) {
      FirebaseFirestore.getInstance()
        .collection("users").document(user.uid)
        .collection("books").whereEqualTo("isbn", book.isbn)
        .get().await().documents.get(0)?.reference?.delete()
    }
    return t?.isSuccessful
  }

  suspend fun uploadBook(user: FirebaseUser, book: Book) {
    Timber.i("#uploadBook: #{book.title}")
    //upload
    if (FirebaseFirestore.getInstance()
        .collection("users").document(user.uid)
        .collection("books").whereEqualTo("isbn", book.isbn)
        .get().await().size() > 0
    ) return

    FirebaseFirestore.getInstance().collection("users").document(user.uid)
      .collection("books")
      .add(book.copy(created_at = Date(), updated_at = Date()))
      .await()
  }

  suspend fun getScraps(user: FirebaseUser, isbn: String): List<Scrap> {
    return (
            FirebaseFirestore.getInstance().collection("users")
              .document(user.uid).collection("scraps").whereEqualTo("isbn", isbn)
              .get().await().documents.map {
              val o = it.toObject(Scrap::class.java) ?: return listOf()
              o.id = it.id; o
            }.filter { it.isbn == isbn }
            )
  }

  suspend fun deleteScrap(user: FirebaseUser, scrap: Scrap) {
    FirebaseFirestore.getInstance().collection("users")
      .document(user.uid).collection("scraps").document(scrap.id).delete()
      .await()
  }

  fun updatePageNumberInScrap(user: FirebaseUser, scrap: Scrap, bookPage: Int) {
    val docRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
    docRef.collection("scraps").document(scrap.id).get().addOnSuccessListener {
      it.reference.update(mapOf("updated_at" to Date(), "pageNumber" to bookPage))
        .addOnSuccessListener {
          //
        }.addOnFailureListener {
          //
        }
    }.addOnFailureListener {
      //
    }
  }
}
