package abc_analytics.com.rebook.Model

import java.util.*

data class Book(
    val id: String = "", val isbn: String = "", val title: String = "",
    val thumbnailUrl: String = "", val created_at: Date = Date(),
    val authors: List<String> = listOf(),
    val updated_at: Date = Date()
)
