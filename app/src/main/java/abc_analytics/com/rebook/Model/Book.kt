package abc_analytics.com.rebook.Model

import java.io.Serializable
import java.util.*

data class Book(
    val isbn: String = "", val title: String = "",
    val thumbnailUrl: String = "", val created_at: Date = Date(),
    val authors: List<String> = listOf(),
    val updated_at: Date = Date(),
    var numScraps: Int = 0,
    var lastImagePath: String = ""
) : Serializable
