package abc_analytics.com.rebook.Activity.Capture

import abc_analytics.com.rebook.*
import abc_analytics.com.rebook.Activity.Login.LoginActivity
import abc_analytics.com.rebook.Activity.ScrapDetail.ScrapDetailActivity
import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.GoogleBook
import abc_analytics.com.rebook.R
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

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
        Log.d(abc_analytics.com.rebook.Activity.Login.TAG, "captureActivity onCreate started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val tmpBook = intent.getParcelableExtra<Book>(EXTRA_BOOK)
        if (tmpBook != null) {
            textViewTitleCapture.text = tmpBook.title
            checkBoxOkTitle.isChecked = true
            isbn = tmpBook.isbn
            title = tmpBook.title
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

    override fun onResume() {
        super.onResume()
        floatingActionButtonCapture.isClickable = true
    }

    private fun setBookInfo(isbnFromBarcode: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "loginしてください", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
        GlobalScope.launch(Dispatchers.Default) {
            val result = withContext(Dispatchers.Default) {
                HttpUtil().httpGET("https://www.googleapis.com/books/v1/volumes?q=${isbnFromBarcode}")
                    .let {
                        val googleBooks = adapter.fromJson(it ?: "{}")
                        Log.d(abc_analytics.com.rebook.Activity.Login.TAG, googleBooks.toString())
                        bookTitle = googleBooks?.items?.get(0)?.volumeInfo?.title ?: ""
                        if (bookTitle == "") {
                            Toast.makeText(
                                this@CaptureActivity,
                                "no book found for $isbnFromBarcode",
                                Toast.LENGTH_LONG
                            ).show()
                            return@let false
                        }
                        thumbnailUrl =
                            googleBooks?.items?.get(0)?.volumeInfo?.imageLinks?.smallThumbnail
                                ?: googleBooks?.items?.get(0)?.volumeInfo?.imageLinks?.thumbnail
                                        ?: "no image"
                        withContext(Dispatchers.Main) {
                            textViewTitleCapture.text = bookTitle
                            checkBoxOkTitle.isChecked = true
                        }
                        authors = googleBooks?.items?.get(0)?.volumeInfo?.authors ?: listOf()
                        isbn = isbnFromBarcode
                        uploadToFirebase(uid = user!!.uid)
                        return@let true
                    }
            }
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.METHOD, "google firebase api")
            firebaseAnalytics.logEvent("upload_book_${isbnFromBarcode}:${result}", bundle)
        }
    }

    private fun uploadToFirebase(uid: String) {
        val db = FirebaseFirestore.getInstance()
        //upload
        val data = mutableMapOf(
            "isbn" to isbn, "user" to uid,
            "localfile_path" to lastImagePath,
            "title" to bookTitle, "thumbnailUrl" to thumbnailUrl, "authors" to authors,
            "created_at" to Date(), "updated_at" to Date(), "numScraps" to 0
        )
        val doc = db.collection("users").document(uid)
        val query = doc.collection("books").whereEqualTo("isbn", isbn)
        query.get().addOnSuccessListener { documents ->
            if (documents.size() < 1) {
                doc.collection("books").add(data)
                    .addOnSuccessListener {
                        Log.d(
                            abc_analytics.com.rebook.Activity.Login.TAG,
                            "DocumentSnapshot added with ID: ${it.id}"
                        )
                        Toast.makeText(this, "upload succeed", Toast.LENGTH_LONG).show()
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "google firebase api")
                        firebaseAnalytics.logEvent("upload_book_success", bundle)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "upload failed", Toast.LENGTH_LONG).show()
                        Log.w(
                            abc_analytics.com.rebook.Activity.Login.TAG,
                            "Error adding document",
                            it
                        )
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
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
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

    private fun updateTransform() {
        fun getViewfinderRotation(): Int {
            return when (viewFinder.display.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
        }

        val matrix = Matrix()
        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f
        // Correct preview output to account for display rotation
        val rotationDegrees = getViewfinderRotation()
        Log.v(abc_analytics.com.rebook.Activity.Login.TAG, "rotation Degree ${rotationDegrees}")
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
        //viewFinder.layoutParams.width = viewFinder.parent.layout
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

        val mOnImageCapturedListner = object : ImageCapture.OnImageCapturedListener() {
            override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                //https://stackoverflow.com/questions/57432526/convert-camerax-captured-imageproxy-to-bitmapI
                //super.onCaptureSuccess(image, rotationDegrees)
                Log.d(
                    abc_analytics.com.rebook.Activity.Login.TAG,
                    "rotationDegrees:${rotationDegrees}"
                )
                var bitmap: Bitmap? = null
                //Imageproxy is closable
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
                if (bitmap != null) {
                    analyzeImage(bitmap!!)
                }
            }

            override fun onError(
                useCaseError: ImageCapture.UseCaseError?, message: String?, cause: Throwable?
            ) {
                Log.d(abc_analytics.com.rebook.Activity.Login.TAG, "error onCaptureListener")
            }
        }
        floatingActionButtonCapture.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            it.isClickable = false
            Log.d(abc_analytics.com.rebook.Activity.Login.TAG, "file first(): ${file.absolutePath}")
            imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        lastImagePath = file.absolutePath
                        val bitmap = BitmapFactory.decodeFile(
                            file.absolutePath,
                            BitmapFactory.Options().also {
                                it.inPreferredConfig = Bitmap.Config.ARGB_8888
                            })
                        analyzeImage(bitmap)
                    }

                    override fun onError(
                        useCaseError: ImageCapture.UseCaseError,
                        message: String,
                        cause: Throwable?
                    ) {
                        Log.d(
                            abc_analytics.com.rebook.Activity.Login.TAG,
                            "failed to capture image in CaptureActivity"
                        )
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

    fun analyzeImage(image: Bitmap) {
        FirebaseApp.initializeApp(this)
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options =
            FirebaseVisionCloudDocumentRecognizerOptions.Builder().setLanguageHints(Arrays.asList("ja", "en")).build()
        val textRecognizer = FirebaseVision.getInstance()
            .getCloudDocumentTextRecognizer(options)
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                Toast.makeText(this@CaptureActivity, "ImageVision OCR Done", Toast.LENGTH_SHORT)
                    .show()
                //if(it != null) {
                val txt = recognizeText(it)
                sendToScrapDetail(txt)
                //}
            }
            .addOnFailureListener { Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show() }
    }


    fun recognizeText(result: FirebaseVisionDocumentText): String {
        if (result == null) {
            Toast.makeText(this, "FirebaseVisionDocumentText is null", Toast.LENGTH_SHORT).show()
        }
        return result.text
    }

    fun sendToScrapDetail(txt: String) {
        val sendIntent = Intent(this@CaptureActivity, ScrapDetailActivity::class.java)
        sendIntent.putExtra(DOC_CONTENT, txt)
        sendIntent.putExtra(IMG_URI, lastImagePath)
        sendIntent.putExtra(ISBN_CONTENT, isbn)
        sendIntent.putExtra(FROM_ACTIVITY, this.localClassName)
        sendIntent.putExtra(TITLE_CONTENT, textViewTitleCapture.text ?: "no title")
        //sendIntent.putExtra("IMG", image )
        startActivityForResult(sendIntent, CAPTURE_DETAIL_INTENT)
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
