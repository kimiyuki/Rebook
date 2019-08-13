package abc_analytics.com.rebook

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request

class HttpUtil {
    //叩きたいREST APIのURLを引数とします
    fun httpGET(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body()?.string()
    }
}