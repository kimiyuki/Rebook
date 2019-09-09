package abc_analytics.com.rebook.Model

data class GoogleBookRes(
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
