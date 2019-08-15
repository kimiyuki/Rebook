package abc_analytics.com.rebook.Model

import java.util.*

class Book(val id: String, val isbn: String, val title: String?, var scraps: ArrayList<Scrap>, bytes: ByteArray) {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}
