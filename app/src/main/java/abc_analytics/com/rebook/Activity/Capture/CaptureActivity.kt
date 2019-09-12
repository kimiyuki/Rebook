package abc_analytics.com.rebook.Activity.Capture

import abc_analytics.com.rebook.*
import abc_analytics.com.rebook.Activity.Login.LoginActivity
import abc_analytics.com.rebook.Activity.Main.MyViewModel
import abc_analytics.com.rebook.Activity.ScrapDetail.ScrapDetailActivity
import abc_analytics.com.rebook.Model.Book
import abc_analytics.com.rebook.Model.Scrap
import abc_analytics.com.rebook.R
import abc_analytics.com.rebook.Repository.getBookInfoFromGoogleAPI
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import kotlinx.android.synthetic.main.content_capture.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class CaptureActivity : AppCompatActivity(), CoroutineScope, LifecycleOwner {
  private var lastImagePath: String = ""
  private var book: Book? = null
  private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }
  private val user: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
  private lateinit var viewModel: MyViewModel
  private val job = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.i("onCreate started")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_capture)
    FirebaseApp.initializeApp(this)
    viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
    user ?: return
    launch(coroExHandler) {
      viewModel.getBooks(user!!).observe(this@CaptureActivity, Observer {
        updateUI()
      })
    }
    book = intent.getParcelableExtra<Book>(EXTRA_BOOK)
    reqPermissionAndStartCamera(view_finder) // Request camera permissions
    view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateTransform() }
    checkBoxOkTitle.setOnCheckedChangeListener { buttonView, isChecked ->
      if (!isChecked) {
        textViewTitleCapture.text = ""
      }
    }
    floatingActionButtonCapture.isClickable = true
  }

  private fun updateUI() {
    book ?: return
    textViewTitleCapture.text = book!!.title
    checkBoxOkTitle.isChecked = true
    title = book!!.title
  }

  private fun setBookInfo(isbnFromBarcode: String) {
    if (user == null) {
      Toast.makeText(this, "loginしてください", Toast.LENGTH_LONG).show()
      val intent = Intent(applicationContext, LoginActivity::class.java)
      startActivity(intent)
    }
    launch(coroExHandler) {
      book = async { getBookInfoFromGoogleAPI(isbnFromBarcode) }.await()
      if (book != null && book!!.title.isNotEmpty()) {
        textViewTitleCapture.text = book!!.title
        checkBoxOkTitle.isChecked = true
        book!!.lastImagePath = lastImagePath
        viewModel.addBook(user!!, book!!)
      }
      Bundle().apply {
        putString(FirebaseAnalytics.Param.METHOD, "google firebase api")
        firebaseAnalytics.logEvent("upload_book_${book?.isbn}", this)
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
      getIsbn = { book?.isbn },
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
      ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS )
    }
  }

  override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>, grantResults: IntArray ) {
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      if (allPermissionsGranted()) {
        view_finder.post { startCamera() }
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
      val parent = view_finder.parent as ViewGroup
      parent.removeView(view_finder)
      parent.addView(view_finder, 0)

      view_finder.surfaceTexture = it.surfaceTexture
      updateTransform()
    }
    return preview
  }

  private fun updateTransform() {
    fun getViewfinderRotation(): Int {
      return when (view_finder.display.rotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
      }
    }

    val matrix = Matrix()
    // Compute the center of the view finder
    val centerX = view_finder.width / 2f
    val centerY = view_finder.height / 2f
    // Correct preview output to account for display rotation
    val rotationDegrees = getViewfinderRotation()
    Log.v(abc_analytics.com.rebook.Activity.Login.TAG, "rotation Degree ${rotationDegrees}")
    matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
    // Finally, apply transformations to our TextureView
    view_finder.setTransform(matrix)
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

    val rotation = view_finder.display.rotation
    Toast.makeText(this, "rotation: ${rotation}", Toast.LENGTH_LONG).show()
    imageCapture.setTargetRotation(rotation)


    floatingActionButtonCapture.setOnClickListener {
      val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
      it.isClickable = false
      imageCapture.takePicture(file,
        object : ImageCapture.OnImageSavedListener {
          override fun onImageSaved(file: File) {
            lastImagePath = file.absolutePath
            val bitmap = BitmapFactory.decodeFile(
              file.absolutePath,
              BitmapFactory.Options().also {
                it.inPreferredConfig = Bitmap.Config.ARGB_8888
              })
            analyzeImage(bitmap, sendToScrapDetail= {txt -> sendToScrapDetail(txt)})
          }
          override fun onError(
            useCaseError: ImageCapture.UseCaseError,
            message: String,
            cause: Throwable?
          ) { }
        })
    }
    return imageCapture
  }
  private fun analyzeImage(image: Bitmap, sendToScrapDetail:(String)->Unit) {
    val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
    val options =
      FirebaseVisionCloudDocumentRecognizerOptions.Builder()
        .setLanguageHints(Arrays.asList("ja", "en")).build()
    val textRecognizer = FirebaseVision.getInstance()
      .getCloudDocumentTextRecognizer(options)
    textRecognizer.processImage(firebaseVisionImage)
      .addOnSuccessListener { sendToScrapDetail(it.text)}
      .addOnFailureListener { }
  }

  private fun sendToScrapDetail(txt: String) {
    val sendIntent = Intent(this@CaptureActivity, ScrapDetailActivity::class.java)
    val scrap = Scrap(
      doc = txt, localImagePath = lastImagePath,
      isbn = book!!.isbn, bookTitle = book!!.title
    )
    sendIntent.putExtra(FROM_ACTIVITY, this.localClassName)
    sendIntent.putExtra(EXTRA_SCRAP, scrap)
    startActivityForResult(sendIntent, CAPTURE_DETAIL_INTENT)
  }
}

