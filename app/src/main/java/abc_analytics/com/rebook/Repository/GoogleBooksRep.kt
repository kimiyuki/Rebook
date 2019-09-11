package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.HttpUtil
import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.GoogleBookRes
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

suspend fun getBookInfoFromGoogleAPI(isbnFromBarcode: String): Book? {
  val ret = withContext(Dispatchers.IO) {
    val ret = async {
      HttpUtil().httpGET("https://www.googleapis.com/books/v1/volumes?q=${isbnFromBarcode}")
    }.await()
      .let {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
          .adapter(GoogleBookRes::class.java).fromJson(it ?: "{}")
      }
    ret
  }
  val googleBook = ret?.items?.get(0)
  val bookTitle = googleBook?.volumeInfo?.title ?: ""
  //if (bookTitle == "") { return null }
  val thumbnailUrl = googleBook?.volumeInfo?.imageLinks?.smallThumbnail
    ?: googleBook?.volumeInfo?.imageLinks?.thumbnail
    ?: "no image"
  val authors: List<String> = googleBook?.volumeInfo?.authors ?: listOf()
  return Book(
    isbn = isbnFromBarcode,
    title = bookTitle,
    thumbnailUrl = thumbnailUrl,
    authors = authors
  )
}
