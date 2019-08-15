package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.GoogleBook
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCaptureConfig.Builder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewFinder: TextureView
    private var lastImagePath: String = ""
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(GoogleBook::class.java)
    //var fifo: Queue<String> = CircularFifoQueue<String>(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewFinder = findViewById(R.id.view_finder)
        reqPermissionAndStartCamera(viewFinder) // Request camera permissions
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateTransform() }

        testButton.setOnClickListener {
            startActivity(Intent(this, DocActivity::class.java))
        }
        Log.d("hello use case", "aaa")
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        Log.d("hello ", "Done")
        checkBoxOkTitle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                textViewTitle.text = ""
            }
        }
    }

    private fun setBookInfo(bookId: String) {
        runBlocking {
            //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackgroundで実行
            async(Dispatchers.Default) {
                HttpUtil().httpGET(
                    "https://www.googleapis.com/books/v1/volumes?q=${bookId}"
                )
            }.await().let {
                val books = adapter.fromJson(it)
                textViewTitle.text = books?.items?.get(0)?.volumeInfo?.title ?: "no book found for $bookId"
                checkBoxOkTitle.isChecked = true
            }
        }
    }

    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        val preview = preview()

        // Build the image capture use case and attach button click listener
        val imageCapture = imageCapture()

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()
        Log.d("hello", "world1")
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            Log.d("hello", "world2")
            analyzer = BarcodeDetector()
        }
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
    }

    private fun reqPermissionAndStartCamera(v: TextureView) {
        if (allPermissionsGranted()) {
            v.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun preview(): Preview {
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        return preview
    }


    private fun imageCapture(): ImageCapture {
        val imageCaptureConfig = Builder()
            .apply {
                setTargetAspectRatio(Rational(1, 1))
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY) }.build()
        val imageCapture = ImageCapture(imageCaptureConfig)
        capture_button.setOnClickListener {
            val file = File( externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg" )
            imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError( error: ImageCapture.UseCaseError, message: String, exc: Throwable? ) {
                        val msg = "Photo capture failed: $message"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.e("CameraXApp", msg)
                        exc?.printStackTrace()
                    }
                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        lastImagePath = file.absolutePath
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d("CameraXApp", msg)
                        //use thread?
                        val options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        analyzeImage(bitmap)
                    }
                })
        }
        return imageCapture
    }

    //fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {
    fun recognizeText(result: FirebaseVisionDocumentText?, image: Bitmap?) {
        if (result == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("aaa result text", result.text.toString())
        val sendIntent = Intent(this@MainActivity, DocActivity::class.java)
        sendIntent.putExtra(DOC_CONTENT, result.text)
        //sendIntent.putExtra("IMG", image )
        startActivityForResult(sendIntent, MAIN_DOC)
        //textViewAnswer.text = result.text.toString()
        Toast.makeText(this@MainActivity, "came to ${result.text} ", Toast.LENGTH_SHORT).show()
    }

    fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("hello analyze", "aaa")
        FirebaseApp.initializeApp(this)
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options =
            FirebaseVisionCloudDocumentRecognizerOptions.Builder().setLanguageHints(Arrays.asList("ja", "en")).build()
        val textRecognizer = FirebaseVision.getInstance()
            .getCloudDocumentTextRecognizer(options)
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "came to success", Toast.LENGTH_SHORT).show()
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
                recognizeText(it, mutableImage)
            }
            .addOnFailureListener { Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show() }
    }



    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    inner class BarcodeDetector : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L
        private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
            val currentTimestamp = System.currentTimeMillis()
            Log.d("hello analyze", degrees.toString())
            // Calculate the average luma no more often than every second
            if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
                val mediaImage = imageProxy?.image
                val imageRotation = degreesToFirebaseRotation(degrees)
                if (mediaImage != null) {
                    val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                    val detector = FirebaseVision.getInstance().visionBarcodeDetector
                    val result = detector.detectInImage(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val valueType = barcode.valueType
                                if (barcode.displayValue?.startsWith("97") == true) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "${ReBook.BARCODE_TYPES[valueType]}:${valueType}:${barcode.displayValue}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    setBookInfo(barcode.displayValue!!)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@MainActivity, "scan failed", Toast.LENGTH_LONG).show()
                        }
                }
                lastAnalyzedTimestamp = currentTimestamp
            }
        }
    }
}
