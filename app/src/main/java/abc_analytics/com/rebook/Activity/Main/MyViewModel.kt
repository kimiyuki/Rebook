package abc_analytics.com.rebook.Activity.Main

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.Repository.FireStoreRep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class MyViewModel : ViewModel(), CoroutineScope {
  var books = MutableLiveData<MutableList<Book>>()
  var scraps = MutableLiveData<MutableList<Scrap>>()
  // var books =_books as LiveData<MutableList<Book>>
  private val job = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  fun addBook(user: FirebaseUser, book: Book) {
    val list = books.value ?: return
    list.add(book)
    viewModelScope.launch {
      //init{} makes list non-null
      FireStoreRep.uploadBook(user = user, book = book)
    }
  }

  fun getBooks(user: FirebaseUser): LiveData<MutableList<Book>> {
    viewModelScope.launch {
      books.value = FireStoreRep.getBooks(user) as MutableList<Book>
    }
    return books
  }

  fun getScraps(user: FirebaseUser, isbn: String): LiveData<MutableList<Scrap>> {
    viewModelScope.launch {
      scraps.value = FireStoreRep.getScraps(user, isbn) as MutableList<Scrap>
    }
    return scraps
  }

  fun deleteBook(user: FirebaseUser, book: Book) {
    val list = books.value ?: return
    list.removeIf { it.id == book.id }
    viewModelScope.launch {
      val ret = FireStoreRep.deleteBook(user = user, book = book)
      if (ret != true) {
        Timber.i("failed to delete")
      }
    }
    books.value = list
  }
}