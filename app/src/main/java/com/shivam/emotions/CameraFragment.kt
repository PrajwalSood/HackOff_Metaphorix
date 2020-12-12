package com.shivam.emotions

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.WorkerThread
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame
import com.shivam.emotions.databinding.FragmentCameraBinding
import com.shivam.emotions.tfmodels.arousal.ArousalNetClassifier
import com.shivam.emotions.tfmodels.arousal.ArousalNetConfig
import com.shivam.emotions.tfmodels.arousal.ClassificationArousal
import com.shivam.emotions.tfmodels.emotion.ClassificationEmotion
import com.shivam.emotions.tfmodels.emotion.PrajuNetClassifier
import com.shivam.emotions.tfmodels.emotion.PrajuNetConfig
import com.shivam.emotions.tfmodels.valence.ClassificationValence
import com.shivam.emotions.tfmodels.valence.ValenceNetClassifier
import com.shivam.emotions.tfmodels.valence.ValenceNetConfig
import com.shivam.emotions.util.EmotionConstants
import com.shivam.emotions.util.EmotionUtil
import com.shivam.emotions.util.PermissionsUtilCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException


class CameraFragment : BaseFragment() {
    private lateinit var cameraBinding: FragmentCameraBinding
    private lateinit var detector: FaceDetector
    private lateinit var prajuNetClassifier: PrajuNetClassifier
    private lateinit var arousalNetClassifier: ArousalNetClassifier
    private lateinit var valenceNetClassifier: ValenceNetClassifier


    private var rotationIMG = 270f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return cameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * Setting the camera to the lifecycle
         */
        cameraBinding.cameraView.setLifecycleOwner(this)


        /**
         * Checking for camera permissions
         */
        permissionUtil.cameraPermission(object : PermissionsUtilCallback {
            override fun onPermissionRequest(granted: Boolean) {
                if (granted) {
                    frameP()
                } else {
                    Toast.makeText(context, "Camera Permission Required!!!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().finish()
                }
            }
        })
        if (cameraBinding.cameraView.facing == Facing.FRONT) {
            rotationIMG = 270f
        } else if (cameraBinding.cameraView.facing == Facing.BACK) {
            rotationIMG = 90f
        }

        cameraBinding.switchBtn.setOnClickListener {
            cameraBinding.cameraView.toggleFacing()
            if (cameraBinding.cameraView.facing == Facing.FRONT) {
                cameraBinding.cameraView.scaleX = -1f
                rotationIMG = 270f
            } else if (cameraBinding.cameraView.facing == Facing.BACK) {
                cameraBinding.cameraView.scaleX = 1f

                rotationIMG = 90f
            }

        }

    }


    private fun frameP() {

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.2f)
            .enableTracking()
            .build()

        detector = FaceDetection.getClient(options)
        loadPrajuNetClassifier()
        loadArousalNetClassifier()
        loadValenceNetClassifier()

        val noFaceSnackBar = Snackbar.make(
            cameraBinding.root,
            "No face detected!!",
            Snackbar.LENGTH_SHORT
        )


        cameraBinding.cameraView.addFrameProcessor(
            @WorkerThread
            fun(@NonNull frame: Frame) {

                if (frame.dataClass == byteArrayOf().javaClass) {
                    visionImg(frame, object : FaceCallback {
                        override fun onFaceFound(isFaceFound: Boolean) {
                            if (isFaceFound) {
                                noFaceSnackBar.dismiss()
                            } else {
                                noFaceSnackBar.show()
                            }
                        }
                    })
                }
            })
    }

    private fun loadPrajuNetClassifier() {
        try {
            prajuNetClassifier =
                PrajuNetClassifier.classifier(
                    requireActivity().assets,
                    PrajuNetConfig.MODEL_FILENAME
                )
        } catch (e: IOException) {
            Timber.e("PrajuNet model couldn't be loaded. Check logs for details.")
            e.printStackTrace()
        }
    }

    private fun loadArousalNetClassifier() {
        try {
            arousalNetClassifier =
                ArousalNetClassifier.classifier(
                    requireActivity().assets,
                    ArousalNetConfig.MODEL_FILENAME
                )
        } catch (e: IOException) {
            Timber.e("ArousalNet model couldn't be loaded. Check logs for details.")
            e.printStackTrace()
        }
    }

    private fun loadValenceNetClassifier() {
        try {
            valenceNetClassifier =
                ValenceNetClassifier.classifier(
                    requireActivity().assets,
                    ValenceNetConfig.MODEL_FILENAME
                )
        } catch (e: IOException) {
            Timber.e("ValenceNet model couldn't be loaded. Check logs for details.")
            e.printStackTrace()
        }
    }


    private fun visionImg(frame: Frame, faceCallback: FaceCallback) {

        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(
            frame.getData(),
            ImageFormat.NV21,
            frame.size.width,
            frame.size.height,
            null
        )
        yuvImage.compressToJpeg(
            Rect(0, 0, frame.size.width, frame.size.height),
            90,
            out
        )
        val imageBytes: ByteArray = out.toByteArray()
        var fullSizeBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        fullSizeBitmap = bitmapUtils.rotateBitmap(fullSizeBitmap, rotationIMG)

        val fullSizeImage = InputImage.fromByteArray(
            frame.getData(),
            frame.size.width,
            frame.size.height,
            frame.rotationToUser,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        )


        detector.process(fullSizeImage)
            .addOnSuccessListener { faces ->
                if (faces.size == 0) {
                    cameraBinding.clAttention.visibility = View.GONE
                    cameraBinding.clEmotion.visibility = View.GONE
                    faceCallback.onFaceFound(false)

                } else if (faces.size >= 1) {
                    faceCallback.onFaceFound(true)
                    cameraBinding.clAttention.visibility = View.VISIBLE
                    cameraBinding.clEmotion.visibility = View.VISIBLE
                    try {
                        val faceBitmap = Bitmap.createBitmap(
                            fullSizeBitmap,
                            faces[0].boundingBox.left,
                            faces[0].boundingBox.top,
                            faces[0].boundingBox.height(),
                            faces[0].boundingBox.width()
                        )



                        GlobalScope.launch(Dispatchers.IO) {
                            val finalBitmap = bitmapUtils.preparePrajuNetBitmap(faceBitmap)

                            val emotions: List<ClassificationEmotion> =
                                prajuNetClassifier.recognizeImage(finalBitmap)
                            val arousals: List<ClassificationArousal> =
                                arousalNetClassifier.recognizeImage(finalBitmap)
                            val valences: List<ClassificationValence> =
                                valenceNetClassifier.recognizeImage(finalBitmap)

                            Timber.d("HHH $emotions \n$arousals \n$valences")

                            try {

                                val cEmotion = EmotionUtil.compoundEmotion(
                                    emotions[0],
                                    arousals[0],
                                    valences[0]
                                )

                                Timber.d("JJJ $cEmotion")

                                launch(Dispatchers.Main) {
                                    val emotion = emotions[0].title
                                    cameraBinding.tvEmotion.text = emotions[0].toString()
                                    cameraBinding.tvCompoundEmotion.text = cEmotion
                                    cameraBinding.ivFace.load(faceBitmap)

                                    val attention = if (valences[0].title == 0) {
                                        "Attention Level: ${
                                            String.format(
                                                "(%.1f%%) ",
                                                (1 - valences[0].confidence) * 100.0f
                                            )
                                        }"
                                    } else {
                                        "Attention Level: ${
                                            String.format(
                                                "(%.1f%%) ",
                                                valences[0].confidence * 100.0f
                                            )
                                        }"
                                    }

                                    val excited = if (arousals[0].title == 0) {

                                        "Excitedness Level : ${
                                            String.format(
                                                "(%.1f%%) ",
                                                (1 - arousals[0].confidence) * 100.0f
                                            )
                                        }"
                                    } else {
                                        "Excitedness Level : ${
                                            String.format(
                                                "(%.1f%%) ",
                                                arousals[0].confidence * 100.0f
                                            )
                                        }"
                                    }
                                    cameraBinding.tvAttention.text = attention
                                    cameraBinding.tvExcited.text = excited

                                    when (emotion) {
                                        EmotionConstants.ANGRY -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_angry)
                                        }
                                        EmotionConstants.CONTEMPT -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_contempt)
                                        }
                                        EmotionConstants.DISGUSTED -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_disgust)
                                        }
                                        EmotionConstants.FEARFUL -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_fearful)
                                        }
                                        EmotionConstants.HAPPY -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_happy)
                                        }
                                        EmotionConstants.NEUTRAL -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_neutral)
                                        }
                                        EmotionConstants.SAD -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_sad)
                                        }
                                        EmotionConstants.SURPRISED -> {
                                            cameraBinding.ivEmotionEmoji.load(R.drawable.ic_emoji_surprised)
                                        }
                                    }

                                }
                            } catch (e: Exception) {
                                Timber.e("Error in Compound Emotion $e")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("Error in Emotion $e")
                    }
                }


                Timber.d("Faces: ${faces.size}")
            }


    }

    interface FaceCallback {
        fun onFaceFound(isFaceFound: Boolean)
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraBinding.cameraView.clearFrameProcessors()
    }


}