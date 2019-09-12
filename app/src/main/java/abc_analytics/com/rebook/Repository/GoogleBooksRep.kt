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
    HttpUtil().httpGET("https://www.googleapis.com/books/v1/volumes?q=${isbnFromBarcode}")
  }.let {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      .adapter(GoogleBookRes::class.java).fromJson(it ?: "{}")
  }
  val googleBook = ret?.items?.get(0)?.volumeInfo ?: return null
  googleBook.title ?: return null
  return Book(
    isbn = isbnFromBarcode,
    title = googleBook.title,
    thumbnailUrl = googleBook.imageLinks?.smallThumbnail
      ?: googleBook.imageLinks?.thumbnail
      ?: "no image",
    authors = googleBook.authors ?: listOf()
  )
}
