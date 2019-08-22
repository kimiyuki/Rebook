package abc_analytics.com.rebook

import android.app.Application
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule




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

@GlideModule
class MyAppGlideModule : AppGlideModule()// leave empty for now
