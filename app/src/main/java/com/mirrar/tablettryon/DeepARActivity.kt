package com.mirrar.tablettryon

import ai.deepar.ar.ARErrorType
import ai.deepar.ar.AREventListener
import ai.deepar.ar.ARTouchInfo
import ai.deepar.ar.ARTouchType
import ai.deepar.ar.CameraResolutionPreset
import ai.deepar.ar.DeepAR
import ai.deepar.ar.DeepARImageFormat
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Message
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.annotations.concurrent.Background
import com.mirrar.tablettryon.LoadImageHandlerThread.LOAD_BITMAP
import com.mirrar.tablettryon.LoadImageHandlerThread.LOAD_DEFAULT_IMAGE_TASK
import com.mirrar.tablettryon.LoadImageHandlerThread.LOAD_IMAGE_FROM_GALLERY_TASK
import com.mirrar.tablettryon.LoadImageHandlerThread.REFRESH_IMAGE_TASK
import com.mirrar.tablettryon.databinding.ActivityDeepAractivityBinding
import com.mirrar.tablettryon.tools.DeepARActivityHelper
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.view.fragment.selfie.SelfieFragment
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import java.util.concurrent.ExecutionException


class DeepARActivity : AppCompatActivity(), SurfaceHolder.Callback, AREventListener {

    private var _binding: ActivityDeepAractivityBinding? = null
    val binding get() = _binding!!

    private val defaultLensFacing = CameraSelector.LENS_FACING_FRONT
    private var surfaceProvider: ARSurfaceProvider? = null
    private val lensFacing = defaultLensFacing
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var buffers: Array<ByteBuffer> = emptyArray()
    private var currentBuffer = 0
    private val NUMBER_OF_BUFFERS: Int = 2
    private val useExternalCameraTexture: Boolean = false

    private var deepAR: DeepAR? = null

    private var currentEffect = 0

    private val screenOrientation = 0

    private var effects: ArrayList<String>? = null
    private var selectedProduct: Product? = null
    private var screenshot = DeepARActivityHelper.SCREENSHOT.SELFIE

    private var recording = false
    private var currentSwitchRecording = false
    private var isCurrentModeCamera = true

    private var width = 0
    private var height = 0

    private val videoFileName: File? = null

    private lateinit var handlerThread: LoadImageHandlerThread
    private lateinit var deepARActivityHelper: DeepARActivityHelper

    override fun onStart() {
        super.onStart()
        width = AR_BITMAP?.width ?: 0
        height = AR_BITMAP?.height ?: 0
        initialize()
    }

    private fun initialize() {
        initializeDeepAR()
        initializeFilters()
        initalizeViews()
    }

    private fun printInitialImage() {
        val msg = Message.obtain(handlerThread.handler)
        msg.what = LOAD_BITMAP
        msg.obj = AR_BITMAP
        msg.sendToTarget()
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        val cacheDir: File = getCacheDir()
        val imageFile = File(
            cacheDir,
            "image_" + System.currentTimeMillis() + ".jpg"
        )

        try {
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos) // Compress and write bitmap
                fos.flush()
                return Uri.fromFile(imageFile) // Return local URI
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun initializeFilters() {
        effects = ArrayList()
        effects!!.add("none")
        effects!!.add("glass.deepar")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initalizeViews() {
        binding.surface.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                when (p1?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        deepAR!!.touchOccurred(
                            ARTouchInfo(
                                p1.getX(),
                                p1.getY(),
                                ARTouchType.Start
                            )
                        )
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        deepAR!!.touchOccurred(
                            ARTouchInfo(
                                p1.getX(),
                                p1.getY(),
                                ARTouchType.Move
                            )
                        )
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        deepAR!!.touchOccurred(
                            ARTouchInfo(
                                p1.getX(),
                                p1.getY(),
                                ARTouchType.End
                            )
                        )
                        return true
                    }
                }
                return false
            }
        })

        binding.surface.holder.addCallback(this)
        binding.surface.visibility = View.GONE
        binding.surface.visibility = View.VISIBLE

    }

    private fun getScreenOrientation(): Int {
        val rotation: Int = getWindowManager().getDefaultDisplay().getRotation()
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        width = dm.widthPixels
        height = dm.heightPixels
        // if the device's natural orientation is portrait:
        val orientation = if ((rotation == Surface.ROTATION_0
                    || rotation == Surface.ROTATION_180) && height > width ||
            (rotation == Surface.ROTATION_90
                    || rotation == Surface.ROTATION_270) && width > height
        ) {
            when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else {
            when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        return orientation
    }

    private fun initializeDeepAR() {
        deepAR = DeepAR(this)
        deepAR!!.setLicenseKey("cc16573f53818d7fa2aa31a48ceb013150e01360d5726afb536c6885ae2cf4fa071952baef77e7b5")
        deepAR!!.initialize(this, this)

        deepAR!!.changeLiveMode(true)
        setupCamera()
    }

    private fun refreshImage() {
        val msg: Message = Message.obtain(handlerThread.handler)
        msg.what = REFRESH_IMAGE_TASK
        msg.sendToTarget()
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                bindImageAnalysis(cameraProvider)
            } catch (e: Exception) {

            }
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindImageAnalysis(cameraProvider: ProcessCameraProvider) {
        val cameraResolutionPreset = CameraResolutionPreset.P1920x1080
        val width: Int ///= binding.surface.width
        val height: Int// = binding.surface.height
        binding.cameraSourcePreview.width = cameraResolutionPreset.height
        binding.cameraSourcePreview.height = cameraResolutionPreset.width
        val orientation = getScreenOrientation()
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            width = cameraResolutionPreset.width
            height = cameraResolutionPreset.height
        } else {
            width = cameraResolutionPreset.height
            height = cameraResolutionPreset.width
        }

        val cameraResolution = Size(width, height)
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        if (useExternalCameraTexture) {
            val preview = Preview.Builder()
                //.setTargetResolution(cameraResolution)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
            if (surfaceProvider == null) {
                surfaceProvider = ARSurfaceProvider(this, deepAR)
            }
            preview.surfaceProvider = surfaceProvider
            surfaceProvider!!.isMirror = lensFacing == CameraSelector.LENS_FACING_FRONT
        } else {
            buffers = Array(NUMBER_OF_BUFFERS) { ByteBuffer.allocate(0) }
            for (i in 0..<NUMBER_OF_BUFFERS) {
                buffers[i] = ByteBuffer.allocateDirect(width * height * 4)
                buffers[i].order(ByteOrder.nativeOrder())
                buffers[i].position(0)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                //.setTargetResolution(cameraResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                imageAnalyzer
            )
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis)
        }
    }

    private val imageAnalyzer =
        ImageAnalysis.Analyzer { image ->
            val buffer = image.planes[0].buffer
            buffer.rewind()
            buffers[currentBuffer].put(buffer)
            buffers[currentBuffer].position(0)
            if (deepAR != null) {
                deepAR!!.receiveFrame(
                    buffers[currentBuffer],
                    image.width, image.height,
                    image.imageInfo.rotationDegrees,
                    lensFacing == CameraSelector.LENS_FACING_FRONT,
                    DeepARImageFormat.RGBA_8888,
                    image.planes[0].pixelStride
                )
            }
            currentBuffer = (currentBuffer + 1) % NUMBER_OF_BUFFERS
            image.close()
        }

    private fun getFilterPath(filterName: String): String? {
        if (filterName == "none") {
            return null
        }
        return "file:///android_asset/$filterName"
    }

    private fun gotoNext() {
        currentEffect = (currentEffect + 1) % effects!!.size
        Log.i("path::", getFilterPath(effects!![currentEffect]) ?: "")
        deepAR!!.switchEffect("effect", getFilterPath(effects!![currentEffect]))
    }

    private fun gotoPrevious() {
        currentEffect = (currentEffect - 1 + effects!!.size) % effects!!.size
        deepAR!!.switchEffect("effect", getFilterPath(effects!![currentEffect]))
    }

    override fun onStop() {
        recording = false
        currentSwitchRecording = false
        var cameraProvider: ProcessCameraProvider? = null
        try {
            cameraProvider = cameraProviderFuture!!.get()
            cameraProvider.unbindAll()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (surfaceProvider != null) {
            surfaceProvider!!.stop()
            surfaceProvider = null
        }
        deepAR!!.release()
        deepAR = null
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (surfaceProvider != null) {
            surfaceProvider!!.stop()
        }
        if (deepAR == null) {
            return
        }
        deepAR!!.setAREventListener(null)
        deepAR!!.release()
        deepAR = null
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDeepAractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deepARActivityHelper = DeepARActivityHelper(this) { s, p ->
            screenshot = s
            selectedProduct = p
            deepAR?.takeScreenshot()
        }
        handlerThread = LoadImageHandlerThread(this)
        handlerThread.start()

        binding.switchMode.setOnClickListener {
            if (!isCurrentModeCamera) {
                deepAR!!.changeLiveMode(true)
                setupCamera()
                setColorAndBackgroundTextView(
                    binding.liveTxt,
                    R.color.white,
                    R.drawable.switch_txt_background
                )
                binding.preview.isVisible = false

                setColorAndBackgroundTextView(binding.staticTxt, R.color.black, 0)
            } else {
                deepAR!!.changeLiveMode(false)
                deepAR!!.setOffscreenRendering(width, height)
                setColorAndBackgroundTextView(binding.liveTxt, R.color.black, 0)
                setColorAndBackgroundTextView(
                    binding.staticTxt,
                    R.color.white,
                    R.drawable.switch_txt_background
                )
                binding.staticTxt.setBackgroundResource(R.drawable.switch_txt_background)
                binding.preview.isVisible = true
            }
            isCurrentModeCamera = !isCurrentModeCamera
        }
    }

    private fun setColorAndBackgroundTextView(tv: TextView, color: Int, background: Int) {
        tv.setTextColor(ContextCompat.getColor(this, color))
        tv.setBackgroundResource(background)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        deepAR!!.setRenderSurface(p0.getSurface(), width, height);
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (deepAR != null) {
            deepAR!!.setRenderSurface(null, 0, 0);
        }
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(supportFragmentManager, fragment.tag)
    }

    override fun screenshotTaken(p0: Bitmap?) {
        if (screenshot == DeepARActivityHelper.SCREENSHOT.SELFIE) {
            openDialogFragment(
                SelfieFragment.newInstance(
                    selectedProduct!!, p0!!
                )
            )
        }
//        val now: CharSequence = DateFormat.format("yyyy_MM_dd_hh_mm_ss", Date())
//        try {
//            val imageFile = File(
//                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                "image_$now.jpg"
//            )
//            val outputStream = FileOutputStream(imageFile)
//            val quality = 100
//            p0!!.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            val contentUri = Uri.fromFile(imageFile)
//            mediaScanIntent.setData(contentUri)
//            sendBroadcast(mediaScanIntent)
//            Toast.makeText(
//                this,
//                "Screenshot " + imageFile.name + " saved.",
//                Toast.LENGTH_SHORT
//            ).show()
//        } catch (e: Throwable) {
//            e.printStackTrace()
//        }
    }

    override fun videoRecordingStarted() {

    }

    override fun videoRecordingFinished() {

    }

    override fun videoRecordingFailed() {

    }

    override fun videoRecordingPrepared() {

    }

    override fun shutdownFinished() {

    }

    override fun initialized() {
        if (deepAR != null && !isCurrentModeCamera) {
            handlerThread.setImageReceiver(deepAR)
            // Load default image
            val msg = Message.obtain(handlerThread.handler)
            msg.what = LOAD_DEFAULT_IMAGE_TASK
            msg.sendToTarget()
            refreshImage()
        }
        //jumpstart masks
        deepAR!!.switchEffect("effect", getFilterPath(effects!!.get(currentEffect)))

    }

    override fun faceVisibilityChanged(p0: Boolean) {

    }

    override fun imageVisibilityChanged(p0: String?, p1: Boolean) {

    }

    override fun frameAvailable(frame: Image?) {
        if (frame != null) {
            val planes: Array<Image.Plane> = frame.getPlanes()
            val buffer: Buffer = planes[0].buffer.rewind()
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmap = createBitmap(width + rowPadding / pixelStride, height)
            bitmap.copyPixelsFromBuffer(buffer)
            binding.preview.setImageBitmap(bitmap)
        }
    }

    override fun error(p0: ARErrorType?, p1: String?) {

    }

    override fun effectSwitched(p0: String?) {

    }
}