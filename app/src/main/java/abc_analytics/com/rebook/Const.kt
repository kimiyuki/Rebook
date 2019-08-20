package abc_analytics.com.rebook

import android.Manifest

const val UsersPATH = "users"       // Firebaseにユーザの表示名を保存するパス
const val ContentsPATH = "contents" // Firebaseに質問を保存するバス
const val AnswersPATH = "answers"   // Firebaseに解答を保存するパス
const val FavoritesPATH = "favorites"// Firebaseにお気に入りを保存するパス
const val DOC_CONTENT = "abc_analytics.com.rebook.doc_content"
const val TITLE_CONTENT = "abc_analytics.com.rebook.title_content"
const val ISBN_CONTENT = "abc_analytics.com.rebook.isbn_content"
const val IMG_URI = "abc_analytics.com.rebook.img_uri"
const val EXTRA_BOOK = "abc_analytics.com.rebook.extra_book"
const val MAIN_DOC_REQUEST_CODE = 100
const val PERMISSIONS_REQUEST_CODE = 111
const val SCRAPLIST_CAPTURE_REQUEST_CODE = 112


// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

