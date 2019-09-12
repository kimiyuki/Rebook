package abc_analytics.com.rebook

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.util.concurrent.TimeUnit

class BarcodeDetector(
  val context: Context, val getIsbn: () -> String?, val firebaseAnalytics: FirebaseAnalytics,
  val checkIfWidgetNoThankyou: () -> Boolean, val setBookInfo: (String) -> Unit
) : ImageAnalysis.Analyzer {
  private var lastAnalyzedTimestamp = 0L
  private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
    0 -> FirebaseVisionImageMetadata.ROTATION_0
    90 -> FirebaseVisionImageMetadata.ROTATION_90
    180 -> FirebaseVisionImageMetadata.ROTATION_180
    270 -> FirebaseVisionImageMetadata.ROTATION_270
    else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
  }

  override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
    //if (checkBoxOkTitle.isChecked) return
    if (checkIfWidgetNoThankyou()) return
    val currentTimestamp = System.currentTimeMillis()
    if (currentTimestamp - lastAnalyzedTimestamp <= TimeUnit.SECONDS.toMillis(1)) return
    val mediaImage = imageProxy?.image
    mediaImage ?: return

    val imageRotation = degreesToFirebaseRotation(degrees)
    val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
    val detector = FirebaseVision.getInstance().visionBarcodeDetector
    detector.detectInImage(image)
      .addOnSuccessListener { barcodes ->
        barcodes.filter { it.displayValue?.startsWith("97") ?: false }.forEach { barcode ->
          val valueType = barcode.valueType
          Toast.makeText(
            context,
            "${ReBook.BARCODE_TYPES[valueType]}:${valueType}:${barcode.displayValue}",
            Toast.LENGTH_LONG
          ).show()
          if (getIsbn() != barcode.displayValue!!) {
            //TODO, needed to change awaitable function?
            setBookInfo(barcode.displayValue!!)
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.METHOD, "camera")
            firebaseAnalytics.logEvent("barcode_captured", bundle)
          }
        }
      }
      .addOnFailureListener {
        Toast.makeText(context, "scan failed", Toast.LENGTH_LONG).show()
      }
    lastAnalyzedTimestamp = currentTimestamp
  }
}
