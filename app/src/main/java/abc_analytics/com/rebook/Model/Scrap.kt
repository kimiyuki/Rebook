package abc_analytics.com.rebook.Model

import java.util.*

data class Scrap(
    val doc: String = "",
    val created_at: Date = Date(),
    val update_at: Date = Date(),
    val imagePath: String = "",
    val page: Int = 0,
    val isbn: String = ""
)

