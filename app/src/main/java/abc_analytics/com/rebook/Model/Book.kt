package abc_analytics.com.rebook.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Book(
    val id: String = "", val isbn: String = "", val title: String = "",
    val thumbnailUrl: String = "", val created_at: Date = Date(),
    val authors: List<String> = listOf(),
    val updated_at: Date = Date(),
    val numScraps: Int = 0
) : Parcelable
