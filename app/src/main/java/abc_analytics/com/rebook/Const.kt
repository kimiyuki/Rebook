package abc_analytics.com.rebook

import android.Manifest

const val DOC_CONTENT = "abc_analytics.com.rebook.doc_content"
const val TITLE_CONTENT = "abc_analytics.com.rebook.title_content"
const val ISBN_CONTENT = "abc_analytics.com.rebook.isbn_content"
const val IMG_URI = "abc_analytics.com.rebook.img_uri"
const val EXTRA_BOOK = "abc_analytics.com.rebook.extra_book"
const val EXTRA_SCRAP = "abc_analytics.com.rebook.extra_scrap"
const val PAGENUMBER_CONTENT = "abc_analytics.com.rebook.extra_pageNumber"
const val WHICH_ACTIVITY = "abc_analytics.com.rebook.from_activity"
const val SCRAP_ID = "abc_analytics.com.rebook.scrap_id"
const val SCRAP_PAGENUMBER = "abc_analytics.com.rebook.scrap_pageNumber"

const val CAPTURE_DETAIL_INTENT = 100
const val SCRAPLIST_DETAIL_INTENT = 101
const val PERMISSIONS_REQUEST_CODE = 111
const val SCRAPLIST_CAPTURE_REQUEST_CODE = 112
// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

