package abc_analytics.com.rebook

import android.app.Application


class ReBook:Application() {
    companion object {
        val BARCODE_TYPES = mapOf(
            Pair(1, "TYPE_UNKNOWN"),
            Pair(2, "TYPE_CONTACT_INFO"),
            Pair(3, "TYPE_EMAIL"),
            Pair(4, "TYPE_ISBN"),
            Pair(5, "TYPE_PHONE"),
            Pair(6, "TYPE_PRODUCT"),
            Pair(7, "TYPE_SMS"),
            Pair(8, "TYPE_TEXT"),
            Pair(9, "TYPE_URL"),
            Pair(10, "TYPE_WIFI"),
            Pair(11, "TYPE_GEO"),
            Pair(12, "TYPE_CALENDAR_EVENT"),
            Pair(13, "TYPE_DRIVER_LICENSE")
        )
    }
}

data class GoogleBook(
    val items: List<Item>?,
    val kind: String?,
    val totalItems: Int?
)

data class Item(
    val accessInfo: AccessInfo,
    val etag: String?,
    val id: String?,
    val kind: String?,
    val saleInfo: SaleInfo?,
    val searchInfo: SearchInfo?,
    val selfLink: String?,
    val volumeInfo: VolumeInfo?
)

data class VolumeInfo(
    val allowAnonLogging: Boolean?,
    val authors: List<String>?,
    val canonicalVolumeLink: String?,
    val contentVersion: String?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val infoLink: String?,
    val language: String?,
    val maturityRating: String?,
    val pageCount: Int?,
    val panelizationSummary: PanelizationSummary?,
    val previewLink: String?,
    val printType: String?,
    val publishedDate: String?,
    val readingModes: ReadingModes?,
    val subtitle: String?,
    val title: String?
)

data class ReadingModes(
    val image: Boolean?,
    val text: Boolean?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)

data class PanelizationSummary(
    val containsEpubBubbles: Boolean?,
    val containsImageBubbles: Boolean?
)

data class IndustryIdentifier(
    val identifier: String?,
    val type: String?
)

data class AccessInfo(
    val accessViewStatus: String?,
    val country: String?,
    val embeddable: Boolean?,
    val epub: Epub?,
    val pdf: Pdf?,
    val publicDomain: Boolean?,
    val quoteSharingAllowed: Boolean?,
    val textToSpeechPermission: String?,
    val viewability: String?,
    val webReaderLink: String?
)

data class Pdf(
    val isAvailable: Boolean?
)

data class Epub(
    val isAvailable: Boolean?
)

data class SaleInfo(
    val country: String?,
    val isEbook: Boolean?,
    val saleability: String?
)

data class SearchInfo(
    val textSnippet: String?
)
