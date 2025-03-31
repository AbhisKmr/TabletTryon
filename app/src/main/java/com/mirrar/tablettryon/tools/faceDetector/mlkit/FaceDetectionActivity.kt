package com.mirrar.tablettryon.tools.faceDetector.mlkit

import android.graphics.PointF
import android.util.Log
import android.util.Size
import android.widget.ImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.mirrar.tablettryon.tools.ARPlacingHandler
import com.mirrar.tablettryon.tools.CanvasView
import kotlin.math.PI


class FaceDetectionActivity(private val glassImage: ImageView) {

    private val alignmentSolver = AlignmentSolver()
    private val arPlacingHandler = ARPlacingHandler(glassImage)

    private fun eulerToDegrees(eulerAngle: Float): Float {
        return eulerAngle * (180f / PI.toFloat())
    }

    fun detectFaces(image: InputImage, cv: CanvasView) {
        // [START set_detector_options]
        alignmentSolver.image = image
        alignmentSolver.canvasSize = Size(cv.width, cv.height)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).enableTracking().build()

        // [END set_detector_options]

        // [START get_detector]
        val detector = FaceDetection.getClient(options)
        // Or, to use the default option:
        // val detector = FaceDetection.getClient();
        // [END get_detector]

        // [START run_detector]
        val result = detector.process(image).addOnSuccessListener { faces ->
            if (!faces.isNullOrEmpty()) {
                val face = faces[0]

                val y = eulerToDegrees(face.headEulerAngleY)
                val x = eulerToDegrees(face.headEulerAngleX)
                val z = eulerToDegrees(face.headEulerAngleZ)

                Log.v("mlkit angle", "${x}: ${y}: ${z}: ")

                val faceContour = face.getContour(FaceContour.FACE)?.points ?: listOf()
//                val faceContour = alignmentSolver.rearrangePoints(
//                    face.getContour(FaceContour.FACE)?.points ?: listOf()
//                )

                val nose = face.getContour(FaceContour.NOSE_BRIDGE)?.points ?: listOf()

//                val nose = alignmentSolver.rearrangePoints(
//                    face.getContour(FaceContour.NOSE_BRIDGE)?.points ?: listOf()
//                )

                val nosePair = nose.map { Pair(it.x, it.y) }

                val contour = faceContour.map { Pair(it.x, it.y) }
                cv.setPoints(contour)

                val bbx = getBoundingBoxDimensions(faceContour)

                arPlacingHandler.placeObject(
                    Size(bbx.first.toInt(), bbx.second.toInt()),
                    nosePair[0].first,
                    nosePair[0].second,
                    arPlacingHandler.angleBetweenPoints(
                        PointF(nosePair[0].first, nosePair[0].second),
                        PointF(nosePair[1].first, nosePair[1].second)
                    )
                )
            }
        }.addOnFailureListener { e ->
            // Task failed with an exception
            // ...
            Log.e("addOnFailureListener:", e.localizedMessage!!.toString())
        }
        // [END run_detector]
    }

    private fun getBoundingBoxDimensions(points: List<PointF>): Pair<Float, Float> {
        if (points.isEmpty()) return Pair(0f, 0f)

        val mostLeft = points.minByOrNull { it.x }?.x ?: 0f
        val mostTop = points.minByOrNull { it.y }?.y ?: 0f
        val mostRight = points.maxByOrNull { it.x }?.x ?: 0f
        val mostBottom = points.maxByOrNull { it.y }?.y ?: 0f

        val width = mostRight - mostLeft
        val height = mostBottom - mostTop

        return Pair(width, height)
    }

    private fun faceOptionsExamples() {
        // [START mlkit_face_options_examples]
        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()

        // Real-time contour detection
        val realTimeOpts =
            FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build()
        // [END mlkit_face_options_examples]
    }

    private fun processFaceList(faces: List<Face>) {
        // [START mlkit_face_list]
        for (face in faces) {
            val bounds = face.boundingBox
            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
            leftEar?.let {
                val leftEarPos = leftEar.position
            }

            // If contour detection was enabled:
            val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
            val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points

            // If classification was enabled:
            if (face.smilingProbability != null) {
                val smileProb = face.smilingProbability
            }
            if (face.rightEyeOpenProbability != null) {
                val rightEyeOpenProb = face.rightEyeOpenProbability
            }

            // If face tracking was enabled:
            if (face.trackingId != null) {
                val id = face.trackingId
            }
        }
        // [END mlkit_face_list]
    }
}