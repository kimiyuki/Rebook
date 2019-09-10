package abc_analytics.com.rebook.Model

import java.io.Serializable
import java.util.*

data class Scrap(
    val doc: String = "",
    val created_at: Date = Date(),
    val updated_at: Date = Date(),
    val imagePath: String = "",
    val localImagePath: String = "",
    val pageNumber: Int = 0,
    val isbn: String = "",
    val bookTitle: String = "",
    var id: String = ""
) : Serializable

