package abc_analytics.com.rebook.Activity.Main

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Repository.FireStoreRep
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class MyViewModel : ViewModel() {
  var books = MutableLiveData<MutableList<Book>>()
  // var books =_books as LiveData<MutableList<Book>>

  @UiThread
  suspend fun addBook(user: FirebaseUser, book: Book) {
    //init{} makes list non-null
    val list = books.value ?: return
    list.add(book)
    FireStoreRep.uploadBook(user = user, book = book)
    //valueに代入があると,外部でobserveしてるものに通知が行く
    books.value = list
  }

  @UiThread
  suspend fun getBooks(user: FirebaseUser): LiveData<MutableList<Book>> {
    return books.also {
      it.value = FireStoreRep.getBooks(user) as MutableList<Book>
    }
  }

  @UiThread
  suspend fun deleteBook(user: FirebaseUser, book: Book) {
    val list = books.value ?: return
    list.removeIf { it.id == book.id }
    FireStoreRep.deleteBook(user = user, book = book)
    books.value = list
  }
}