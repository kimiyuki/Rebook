package abc_analytics.com.rebook.Activity.Main

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.Repository.FireStoreRep
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MyViewModel : ViewModel(), CoroutineScope {
  var books = MutableLiveData<MutableList<Book>>()
  var scraps = MutableLiveData<MutableList<Scrap>>()
  // var books =_books as LiveData<MutableList<Book>>
  private val job = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  @UiThread
  fun addBook(user: FirebaseUser, book: Book) {
    //init{} makes list non-null
    val list = books.value ?: return
    list.add(book)
    launch {
      FireStoreRep.uploadBook(user = user, book = book)
    }
    //valueに代入があると,外部でobserveしてるものに通知が行く
    books.value = list
  }

  @UiThread
  fun getBooks(user: FirebaseUser): LiveData<MutableList<Book>> {
    return books.also {
      launch {
        it.value = FireStoreRep.getBooks(user) as MutableList<Book>
      }
    }
  }

  @UiThread
  fun getScraps(user: FirebaseUser, isbn: String): LiveData<MutableList<Scrap>> {
    return scraps.also {
      launch {
        it.value = FireStoreRep.getScraps(user, isbn) as MutableList<Scrap>
      }
    }
  }

  @UiThread
  fun deleteBook(user: FirebaseUser, book: Book) {
    val list = books.value ?: return
    list.removeIf { it.id == book.id }
    launch {
      FireStoreRep.deleteBook(user = user, book = book)
    }
    books.value = list
  }
}