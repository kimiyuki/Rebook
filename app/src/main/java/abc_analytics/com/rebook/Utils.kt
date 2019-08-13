package abc_analytics.com.rebook

import android.util.Log
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.json.JSONObject
import java.io.IOException

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
