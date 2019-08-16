package abc_analytics.com.rebook

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.json.JSONObject
import java.io.IOException

fun getContentsInfo(context: Context): Array<Uri> {
    // 画像の情報を取得する
    val resolver = context.contentResolver
    var ret: Array<Uri> = arrayOf(Uri.EMPTY)
    val cursor = resolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
        arrayOf("title", "_id"), // 項目(null = 全項目)
        null, // フィルタ条件(null = フィルタなし)
        null, // フィルタ用パラメータ
        null // ソート (null ソートなし)
    )
    Log.e("hello", cursor.count.toString())
    cursor ?: return ret
    if (cursor.moveToFirst()) {
        do {
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            //Log.d("ANDROID", "URI : " + imageUri.toString())
            Log.d("file path", cursor.getString(0))
            //Log.d("file date", cursor.getString(6))
            //Log.d("file description", cursor.getType(9).toString())
        } while (cursor.moveToNext())
    }
    cursor.close()
    return ret
}

fun getBookInfo(code9: String): String {
    val title = "aaa bbb"
    val url = "https://www.googleapis.com/books/v1/volumes?q=${code9}"
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(req: Request, e: IOException) {
            Log.d("hello", e.message)
        }

        override fun onResponse(response: Response) {
            val json = JSONObject(response.body().string())
            val title = json.getJSONArray("items").getJSONObject(0)
                .getJSONObject("volumeInfo").getJSONArray("title")
        }
    })
    return title
}
