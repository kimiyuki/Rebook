package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.Model.Scrap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

object CloudStorageRep {
  val TAG = "CloudStorageRepository"
  private val storageRef = FirebaseStorage.getInstance().reference

  suspend fun uploadFile(fpath: String, scrap: Scrap) {
    Timber.i("uploadFile")
    storageRef.child("${System.currentTimeMillis()}.jpg")
      .putFile(Uri.fromFile(File(scrap.localImagePath))).await()
  }

  suspend fun downLoadFile(fpath: String): File {
    Timber.i("downFile")
    return File.createTempFile("images", "jpg").also {
      storageRef.child(fpath).getFile(it).await()
    }
  }
}
