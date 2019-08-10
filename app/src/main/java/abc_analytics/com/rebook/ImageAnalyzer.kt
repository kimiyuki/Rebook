package abc_analytics.com.rebook

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit


class CloudDocumentTextRecognitionProcessor : VisionProcessorBase<FirebaseVisionDocumentText>() {

}
private class ImageAnalyzerForDoc : ImageAnalysis.Analyzer {
    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
        val mediaImage = imageProxy?.image
        val imageRotation = degreesToFirebaseRotation(degrees)
        if (mediaImage != null) {
            val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            // Pass image to an ML Kit Vision API
            // to provide language hints to assist with language detection:
            // See https://cloud.google.com/vision/docs/languages for supported languages
            val options = FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("ja")).build()
            val detector = FirebaseVision.getInstance().getCloudDocumentTextRecognizer(options)
            detector.processImage(image)
                .addOnSuccessListener {
                    Log.d("hello detector", "success")
                }
                .addOnFailureListener {
                    Log.d("hello detector error", it.toString())
                }
        }
    }
}
