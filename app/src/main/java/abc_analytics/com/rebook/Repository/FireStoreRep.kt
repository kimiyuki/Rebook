package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

object FireStoreRep {
  val TAG = "FIREBASE_REPOSITORY"

  private val ref: DocumentReference by lazy {
    runCatching {
      FirebaseFirestore.getInstance().collection("users").document(
        FirebaseAuth.getInstance().currentUser!!.uid
      )
    }.getOrThrow()
  }

  // get saved addresses from firebasion("users").document(user.uid)e
  suspend fun getBooks(user: FirebaseUser): List<Book?> {
    //throw UnsupportedOperationException()
    val tasks = withContext(Dispatchers.IO) {
      listOf(
        ref.collection("books").get(),
        ref.collection("scraps").get()
      )
    }
    val books = tasks.get(0).await().documents.map { it.toObject(Book::class.java) }
    Timber.i(books.size.toString())
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
    runCatching {
      ref.collection("books").whereEqualTo("isbn", book.isbn)
        .get().await().first().reference.delete().await()
    }.let { return it.isSuccess }
  }

  suspend fun addBook(user: FirebaseUser, book: Book): Boolean {
    Timber.i("#addBook: #{book.title}")
    ref.collection("books")
      .document(book.isbn).get().await().exists() && return false
    ref.set(book.copy(created_at = Date(), updated_at = Date()))
      .let { t ->
        runCatching { Tasks.await(t) }.let { return it.isSuccess }
      }
  }

  suspend fun getScraps(user: FirebaseUser, isbn: String): List<Scrap> {
    ref.collection("scraps").whereEqualTo("isbn", isbn)
      .get().await().documents.map {
      val o = it.toObject(Scrap::class.java) ?: return listOf()
      o.firestoreId = it.id; o
    }.filter { it.isbn == isbn }.let { return it }
  }

  suspend fun deleteScrap(user: FirebaseUser, scrap: Scrap) {
    ref.collection("scraps").document(scrap.firestoreId).delete().await()
  }

  fun updatePageNumberInScrap(user: FirebaseUser, scrap: Scrap, bookPage: Int) {
    ref.collection("scrapRoot")
      .document(scrap.isbn)
      .collection("scraps")
      .document(scrap.id)
      .update(mapOf("updated_at" to Date(), "pageNumber" to bookPage))
      .addOnSuccessListener {
        //
      }.addOnFailureListener {
        //
      }
  }
}
