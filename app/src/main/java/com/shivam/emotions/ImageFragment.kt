package com.shivam.emotions

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.shivam.emotions.adapter.ImageEmotionAdapter
import com.shivam.emotions.databinding.FragmentImageBinding
import com.shivam.emotions.model.ImageEmotionModel
import com.shivam.emotions.tfmodels.arousal.ArousalNetClassifier
import com.shivam.emotions.tfmodels.arousal.ArousalNetConfig
import com.shivam.emotions.tfmodels.arousal.ClassificationArousal
import com.shivam.emotions.tfmodels.emotion.ClassificationEmotion
import com.shivam.emotions.tfmodels.emotion.PrajuNetClassifier
import com.shivam.emotions.tfmodels.emotion.PrajuNetConfig
import com.shivam.emotions.tfmodels.valence.ClassificationValence
import com.shivam.emotions.tfmodels.valence.ValenceNetClassifier
import com.shivam.emotions.tfmodels.valence.ValenceNetConfig
import com.shivam.emotions.util.EmotionUtil
import com.shivam.emotions.util.ImagePickerUtil
import com.shivam.emotions.util.PermissionsUtilCallback

import timber.log.Timber
import java.io.IOException


class ImageFragment : ImagePickerUtil() {
    private lateinit var imageBinding: FragmentImageBinding
    private lateinit var detector: FaceDetector
    private lateinit var imageEmotionAdapter: ImageEmotionAdapter
    private lateinit var prajuNetClassifier: PrajuNetClassifier
    private lateinit var arousalNetClassifier: ArousalNetClassifier
    private lateinit var valenceNetClassifier: ValenceNetClassifier



    private val args by navArgs<ImageFragmentArgs>()
    private var isIntentUri = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        imageBinding = FragmentImageBinding.inflate(inflater, container, false)
        return imageBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("KKK")

        imageBinding.fabShare.visibility = View.GONE
        if (args.fileURI != null) {

            Timber.d("KKK k")
            isIntentUri = true
            imagePathUriString = args.fileURI
            imageBitmap =
                MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    Uri.parse(args.fileURI)
                )

            handleImage()


        }
        imageBinding.btnImageChoose.setOnClickListener {

            permissionUtil.storagePermission(object : PermissionsUtilCallback {
                override fun onPermissionRequest(granted: Boolean) {
                    if (granted) {
                        imageBitmap = null
                        imagePathUriString = null
                        capturePickImageVideoDialog()
                    } else {
                        Toast.makeText(
                            context,
                            "Storage Permission is needed to pick and save an image",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            })

        }


    }


    private fun loadPrajuNetClassifier() {
        try {
            prajuNetClassifier =
                PrajuNetClassifier.classifier(
                    requireActivity().assets,
                    PrajuNetConfig.MODEL_FILENAME
                )
        } catch (e: IOException) {
            Timber.d("PrajuNet model couldn't be loaded. Check logs for details.")
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == RC_CAPTURE_IMAGE || requestCode == RC_PICK_IMAGE)
            && resultCode == RESULT_OK && imageBitmap != null && imagePathUriString != null
        ) {
            handleImage()
        } else {
            Toast.makeText(context, "Error loading Image!!", Toast.LENGTH_SHORT).show()
        }


    }

    private fun handleImage() {
        imageBinding.btnAnalyseImage.visibility = View.VISIBLE
        imageBinding.rvEmotions.visibility = View.GONE


        imageBitmap = bitmapUtils.getStraightBitmap(
            imageBitmap!!,
            imagePathUriString!!,
            isIntentUri,
            requireActivity()
        )
        isIntentUri = false
        Timber.d("Image URI: $imagePathUriString ")

        /**
         * PICK path aata hai --> Out share working
         * CAPTURE path aata hai --> Out share working
         * SHARE content aata hai--> Out share working
         *
         */
        val scaledBitmap = bitmapUtils.scaleBitmap(imageBitmap!!, 2048, 2048)
        imageBinding.ivUserImage.load(scaledBitmap)

        imageBinding.btnAnalyseImage.setOnClickListener {

            if (this::detector.isInitialized) {
                detector.close()
            }
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.2f)
                .enableTracking()
                .build()

            detector = FaceDetection.getClient(options)
            val faceList: ArrayList<ImageEmotionModel> = ArrayList()

            imageEmotionAdapter = ImageEmotionAdapter(faceList)
            imageBinding.rvEmotions.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            imageBinding.rvEmotions.adapter = imageEmotionAdapter

            if (!this::prajuNetClassifier.isInitialized)
                loadPrajuNetClassifier()
            if (!this::valenceNetClassifier.isInitialized)
                loadValenceNetClassifier()
            if (!this::arousalNetClassifier.isInitialized)
                loadArousalNetClassifier()

            var drawBitmap = scaledBitmap
            detector.process(InputImage.fromBitmap(scaledBitmap, 0))
                .addOnSuccessListener { faces ->
                    Timber.d("HHH ${faces.size}")
                    if (faces.size == 0) {
                        Snackbar.make(
                            imageBinding.root,
                            "No face detected!!",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(
                                "Retry"
                            ) {
                                capturePickImageVideoDialog()
                            }
                            .show()
                    } else {

                        for (face in faces) {
                            try {
                                val faceBitmap = Bitmap.createBitmap(
                                    scaledBitmap,
                                    face.boundingBox.left,
                                    face.boundingBox.top,
                                    face.boundingBox.height(),
                                    face.boundingBox.width()
                                )
                                val rectF = RectF(
                                    face.boundingBox.left.toFloat(),
                                    face.boundingBox.top.toFloat(),
                                    face.boundingBox.right.toFloat(),
                                    face.boundingBox.bottom.toFloat()
                                )
                                drawBitmap = bitmapUtils.drawRectangle(drawBitmap, rectF)

                                val finalBitmap = bitmapUtils.preparePrajuNetBitmap(faceBitmap)

                                val emotions: List<ClassificationEmotion> =
                                    prajuNetClassifier.recognizeImage(finalBitmap)

                                val arousals: List<ClassificationArousal> =
                                    arousalNetClassifier.recognizeImage(finalBitmap)

                                val valences: List<ClassificationValence> =
                                    valenceNetClassifier.recognizeImage(finalBitmap)
                                var cEmotion = ""
                                try {
                                    cEmotion = EmotionUtil.compoundEmotion(
                                        emotions[0],
                                        arousals[0],
                                        valences[0]
                                    )


                                } catch (e: Exception) {
                                    Timber.e("Error in Compound Emotion $e")

                                }

                                if (emotions.isNotEmpty()) {
                                    faceList.add(
                                        ImageEmotionModel(
                                            face.trackingId!!,
                                            faceBitmap,
                                            emotions[0].toString(),
                                            cEmotion
                                        )
                                    )
                                }
                                else{
                                    faceList.add(
                                        ImageEmotionModel(
                                            face.trackingId!!,
                                            faceBitmap,
                                            "No conclusive Emotion",
                                            ""
                                        )
                                    )
                                }

                                imageBinding.ivUserImage.load(drawBitmap)
                                imageBinding.rvEmotions.visibility = View.VISIBLE
                                imageEmotionAdapter.notifyDataSetChanged()


                            } catch (e: Exception) {
                                Timber.e(e)
                                Snackbar.make(
                                    imageBinding.root,
                                    "Some faces not detected!!",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction(
                                        "Retry"
                                    ) {
                                        capturePickImageVideoDialog()
                                    }
                                    .show()
                            }
                        }

                    }

                }

        }

        imageBinding.fabShare.visibility = View.VISIBLE

        imageBinding.fabShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "image/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePathUriString))

            requireActivity().startActivities(
                arrayOf(
                    Intent.createChooser(
                        sharingIntent,
                        "Share with"
                    )
                )
            )
        }
    }





}