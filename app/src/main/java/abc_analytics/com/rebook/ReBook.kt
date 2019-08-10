package abc_analytics.com.rebook

import android.app.Application
import com.google.firebase.FirebaseApp

class ReBook:Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}