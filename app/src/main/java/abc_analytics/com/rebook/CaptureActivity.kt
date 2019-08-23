package abc_analytics.com.rebook

import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.GoogleBook
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCaptureConfig.Builder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.content_capture.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
//private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
//private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CaptureActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewFinder: TextureView
    private var lastImagePath: String = ""
    private var thumbnailUrl: String = ""
    private var bookTitle: String = ""
    private var authors: List<String> = listOf<String>()
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(GoogleBook::class.java)
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isbn: String = ""
    //var fifo: Queue<String> = CircularFifoQueue<String>(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "captureActivity onCreate started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val tmpBook = intent.getParcelableExtra<Book>(EXTRA_BOOK)
        if (tmpBook != null) {
            textViewTitleCapture.text = tmpBook.title
            checkBoxOkTitle.isChecked = true
            isbn = tmpBook.isbn
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //setSupportActionBar(toolbar)
        viewFinder = findViewById(R.id.view_finder)

        reqPermissionAndStartCamera(viewFinder) // Request camera permissions
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateTransform() }

        checkBoxOkTitle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                textViewTitleCapture.text = ""
            }
        }
    }

    private fun setBookInfo(isbnFromBarcode: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "loginしてください", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
        runBlocking {
            //Mainスレッドでネットワーク関連処理を実行するとエラーになるためBackground(Default)で実行
            async(Dispatchers.Default) {
                HttpUtil().httpGET("https://www.googleapis.com/books/v1/volumes?q=${isbnFromBarcode}")
            }.await().let {
                val googleBooks = adapter.fromJson(it)
                bookTitle =
                    googleBooks?.items?.get(0)?.volumeInfo?.title
                        ?: "no book found for $isbnFromBarcode"
                thumbnailUrl =
                    googleBooks?.items?.get(0)?.volumeInfo?.imageLinks?.smallThumbnail ?: "no image"
                authors = googleBooks?.items?.get(0)?.volumeInfo?.authors ?: listOf()
                textViewTitleCapture.text = "${bookTitle}"
                checkBoxOkTitle.isChecked = true
                isbn = isbnFromBarcode
            }
        }
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.METHOD, "google book api")
        firebaseAnalytics.logEvent("external_api_success", bundle)
    }

    private fun uploadToFirebase(uid: String) {
        val db = FirebaseFirestore.getInstance()
        //upload
        val data = mutableMapOf(
            "isbn" to isbn, "user" to uid,
            "title" to bookTitle, "thumbnailUrl" to thumbnailUrl, "authors" to authors,
            "created_at" to Date(), "updated_at" to Date(), "numScraps" to 0
        )
        val doc = db.collection("users").document(uid)
        val query = doc.collection("books").whereEqualTo("isbn", isbn)
        query.get().addOnSuccessListener { documents ->
            if (documents.size() < 1) {
                doc.collection("books").add(data)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${it.id}")
                        Toast.makeText(this, "upload succeed", Toast.LENGTH_LONG).show()
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "google firebase api")
                        firebaseAnalytics.logEvent("upload_book_success", bundle)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "upload failed", Toast.LENGTH_LONG).show()
                        Log.w(TAG, "Error adding document", it)
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "google firebase api")
                        firebaseAnalytics.logEvent("upload_book_failure", bundle)
                    }
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
            val analyzerThread = HandlerThread("barCodeDetector").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()
        //val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
        val analyzer = BarcodeDetector(
            context = this,
            getIsbn = { isbn },
            firebaseAnalytics = firebaseAnalytics,
            checkIfWidgetNoThankyou = { checkBoxOkTitle.isChecked },
            setBookInfo = { isbn -> setBookInfo(isbn) }
        )
        val imageAnalyzer = ImageAnalysis(analyzerConfig)
        imageAnalyzer.analyzer = analyzer
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalyzer)
    }

    private fun reqPermissionAndStartCamera(v: TextureView) {
        if (allPermissionsGranted()) {
            v.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()
        val imageCapture = ImageCapture(imageCaptureConfig) //same name of this constructing function?
        //val display = DisplayMetrics()
        //imageCapture.setTargetRotation(90)

        val rotation = viewFinder.display.rotation
        Toast.makeText(this, "rotation: ${rotation}", Toast.LENGTH_LONG).show()
        imageCapture.setTargetRotation(rotation)


        floatingActionButtonCapture.setOnClickListener {
            imageCapture.takePicture(object : ImageCapture.OnImageCapturedListener() {
                override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                    //https://stackoverflow.com/questions/57432526/convert-camerax-captured-imageproxy-to-bitmapI
                    //super.onCaptureSuccess(image, rotationDegrees)
                    Log.d(TAG, "rotationDegrees:${rotationDegrees}")
                    var bitmap: Bitmap? = null
                    //take advantage of closable image(imageproxy) object property
                    image.use { image ->
                        bitmap = image?.let { imageProxyToBitmap(it) } ?: return
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        val exif = ExifInterface(bytes.inputStream())
                        val filename = System.currentTimeMillis().toString() + ".jpg"
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, filename)
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    }
                    analyzeImage(bitmap)
                }

                override fun onError(
                    useCaseError: ImageCapture.UseCaseError?,
                    message: String?,
                    cause: Throwable?
                ) {
                    //super.onError(useCaseError, message, cause)
                    Log.d(TAG, "error onCaptureListener")
                }
                })
        }
        return imageCapture
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {
    fun recognizeText(result: FirebaseVisionDocumentText?) {
        if (result == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show(); return
        }
        val sendIntent = Intent(this@CaptureActivity, ScrapDetailActivity::class.java)
        sendIntent.putExtra(DOC_CONTENT, result.text)
        sendIntent.putExtra(IMG_URI, lastImagePath)
        sendIntent.putExtra(ISBN_CONTENT, isbn)
        sendIntent.putExtra(FROM_ACTIVITY, this.localClassName)
        sendIntent.putExtra(TITLE_CONTENT, textViewTitleCapture.text ?: "no title")

        //sendIntent.putExtra("IMG", image )
        startActivityForResult(sendIntent, CAPTURE_DETAIL_INTENT)
        //textViewAnswer.text = result.text.toString()
        Toast.makeText(this@CaptureActivity, "came to ${result.text} ", Toast.LENGTH_SHORT).show()
    }

    fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseApp.initializeApp(this)
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options =
            FirebaseVisionCloudDocumentRecognizerOptions.Builder().setLanguageHints(Arrays.asList("ja", "en")).build()
        val textRecognizer = FirebaseVision.getInstance()
            .getCloudDocumentTextRecognizer(options)
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                Toast.makeText(this@CaptureActivity, "came to success", Toast.LENGTH_SHORT).show()
                recognizeText(it)
            }
            .addOnFailureListener { Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show() }
    }

    private fun getViewfinderRotation(): Int {
        return when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    private fun updateTransform() {
        val matrix = Matrix()
        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f
        // Correct preview output to account for display rotation
        val rotationDegrees = getViewfinderRotation()
        Log.v(TAG, "rotation Degree ${rotationDegrees}")
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
        //viewFinder.layoutParams.width = viewFinder.parent.layout
    }
}

class BarcodeDetector(
    val context: Context, val getIsbn: () -> String, val firebaseAnalytics: FirebaseAnalytics,
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
                                    context,
                                    "${ReBook.BARCODE_TYPES[valueType]}:${valueType}:${barcode.displayValue}",
                                    Toast.LENGTH_LONG
                                ).show()
                                if (getIsbn() != barcode.displayValue!!) {
                                    setBookInfo(barcode.displayValue!!)
                                }
                                val bundle = Bundle()
                                bundle.putString(FirebaseAnalytics.Param.METHOD, "camera")
                                firebaseAnalytics.logEvent("barcode_captured", bundle)
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "scan failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}
