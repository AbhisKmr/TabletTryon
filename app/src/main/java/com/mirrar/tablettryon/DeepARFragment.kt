package com.mirrar.tablettryon

import ai.deepar.ar.ARErrorType
import ai.deepar.ar.AREventListener
import ai.deepar.ar.ARTouchInfo
import ai.deepar.ar.ARTouchType
import ai.deepar.ar.CameraResolutionPreset
import ai.deepar.ar.DeepAR
import ai.deepar.ar.DeepARImageFormat
import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Handler
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.mirrar.tablettryon.databinding.FragmentDeepARBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date
import java.util.concurrent.ExecutionException


class DeepARFragment : Fragment(), SurfaceHolder.Callback, AREventListener {

    private var _binding: FragmentDeepARBinding? = null
    private val binding get() = _binding!!

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

    private var recording = false
    private var currentSwitchRecording = false

    private var width = 0
    private var height = 0

    private val videoFileName: File? = null

    override fun onStart() {
        super.onStart()
        initialize()
        Handler().postDelayed({

        }, 1000)
    }

    private fun initialize() {
        initializeDeepAR()
        initializeFilters()
        initalizeViews()
    }

    private fun initializeFilters() {
        effects = ArrayList()
        effects!!.add("none")
        effects!!.add("glass.deepar")
        effects!!.add("viking_helmet.deepar")
        effects!!.add("MakeupLook.deepar")
        effects!!.add("Split_View_Look.deepar")
        effects!!.add("Emotions_Exaggerator.deepar")
        effects!!.add("Emotion_Meter.deepar")
        effects!!.add("Stallone.deepar")
        effects!!.add("flower_face.deepar")
        effects!!.add("galaxy_background.deepar")
        effects!!.add("Humanoid.deepar")
        effects!!.add("Neon_Devil_Horns.deepar")
        effects!!.add("Ping_Pong.deepar")
        effects!!.add("Pixel_Hearts.deepar")
        effects!!.add("Snail.deepar")
        effects!!.add("Hope.deepar")
        effects!!.add("Vendetta_Mask.deepar")
        effects!!.add("testGlass.deepar")
        effects!!.add("burning_effect.deepar")
        effects!!.add("Elephant_Trunk.deepar")
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
        val rotation: Int = requireActivity().getWindowManager().getDefaultDisplay().getRotation()
        val dm = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(dm)
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
        deepAR = DeepAR(requireContext())
        deepAR!!.setLicenseKey("cc16573f53818d7fa2aa31a48ceb013150e01360d5726afb536c6885ae2cf4fa071952baef77e7b5")
        deepAR!!.initialize(requireContext(), this)
        setupCamera()
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                bindImageAnalysis(cameraProvider)
            } catch (e: Exception) {

            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }


    private fun bindImageAnalysis(cameraProvider: ProcessCameraProvider) {
        val cameraResolutionPreset = CameraResolutionPreset.P1920x1080
        val width: Int
        val height: Int
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
                .setTargetResolution(cameraResolution)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
            if (surfaceProvider == null) {
                surfaceProvider = ARSurfaceProvider(requireContext(), deepAR)
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
                .setTargetResolution(cameraResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(requireContext()),
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

    override fun onDestroyView() {
        super.onDestroyView()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeepARBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun screenshotTaken(p0: Bitmap?) {
        val now: CharSequence = DateFormat.format("yyyy_MM_dd_hh_mm_ss", Date())
        try {
            val imageFile = File(
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "image_$now.jpg"
            )
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            p0!!.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(imageFile)
            mediaScanIntent.setData(contentUri)
            requireActivity().sendBroadcast(mediaScanIntent)
            Toast.makeText(
                requireContext(),
                "Screenshot " + imageFile.name + " saved.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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
        deepAR!!.switchEffect("effect", getFilterPath(effects!!.get(currentEffect)));

    }

    override fun faceVisibilityChanged(p0: Boolean) {

    }

    override fun imageVisibilityChanged(p0: String?, p1: Boolean) {

    }

    override fun frameAvailable(p0: Image?) {

    }

    override fun error(p0: ARErrorType?, p1: String?) {

    }

    override fun effectSwitched(p0: String?) {

    }
}