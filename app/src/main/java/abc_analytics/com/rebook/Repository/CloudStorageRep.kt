package abc_analytics.com.rebook.Repository

import abc_analytics.com.rebook.Model.Scrap
import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

object CloudStorageRep {
  val TAG = "CloudStorageRepository"
  private val storageRef = FirebaseStorage.getInstance().reference

  fun uploadFile(user: FirebaseUser, scrap: Scrap): String {
    Timber.i("uploadFile")
    var imagePath: String
    storageRef.child(user.uid).child("images").child(scrap.isbn)
      .child("${System.currentTimeMillis()}.jpg").also {
        imagePath = it.path
      }
      .putFile(Uri.fromFile(File(scrap.localImagePath)))
    return imagePath
  }

  suspend fun downLoadFile(fpath: String): File {
    Timber.i("downFile")
    return File.createTempFile("images", "jpg").also {
      storageRef.child(fpath).getFile(it).await()
    }
  }
}
