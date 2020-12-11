package com.shivam.emotions.util

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shivam.emotions.BaseFragment
import com.shivam.emotions.R
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


open class ImagePickerUtil : BaseFragment() {

    var RC_CAPTURE_IMAGE = 111
    val RC_PICK_IMAGE = 112
    val RC_PICK_VIDEO = 113

    private var isVideoEnabled: Boolean = false


    private val IMAGE_DIRECTORY = "/amlohApps"
    var imagePathUriString: String? = null
    var selectedVideoPath: String? = null
    var imageBitmap: Bitmap? = null
    var videoThumb: Bitmap? = null

    fun capturePickImageVideoDialog() {
        val items =
            arrayOf<CharSequence>(
                "Take Photo",
                "Choose Photo"
            )


        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.AboutUsAlertDialogStyle
        ).setTitle("Select an Option")
            .setItems(items) { dialog, item ->
                when (item) {
                    0 -> capturePictureIntent()
                    1 -> pickImageIntent()
//                2 -> pickVideoIntent()
                    else -> dialog.dismiss()
                }
            }

            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun capturePictureIntent() {
        permissionUtil.cameraPermission(object : PermissionsUtilCallback {
            @SuppressLint("QueryPermissionsNeeded")
            override fun onPermissionRequest(granted: Boolean) {
                if (granted) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null)
                        startActivityForResult(takePictureIntent, RC_CAPTURE_IMAGE)
                } else {
                    Toast.makeText(context, "Camera Permission Required!!!", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }

        })

    }

    private fun pickImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_PICK_IMAGE)
    }

    private fun pickVideoIntent() {
        val i =
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(i, "Select Video"), RC_PICK_VIDEO)
    }

    private fun saveImage(myBitmap: Bitmap?): String {
        val bytes = ByteArrayOutputStream()
        myBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists())
            wallpaperDirectory.mkdirs()

        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            imagePathUriString = f.absolutePath
            Timber.d("File Saved::--->%s", f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Getting the imagesFrom the Camera Intent
        if (requestCode == RC_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            try {
                imageBitmap = data!!.extras!!.get("data") as Bitmap
//                imagePathUriString = saveImage(imageBitmap)

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                val tempUri = getImageUri(imageBitmap!!)

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                val finalFile = File(getRealPathFromURI(tempUri!!)!!)
                imagePathUriString = finalFile.absolutePath


            } catch (e: Exception) {
                Timber.e("Error getting Camera Bitmap! ")
                e.printStackTrace()
            }

        }


        //Getting the imagesFrom the Gallery
        if (requestCode == RC_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            try {
                imageBitmap =
                    Images.Media.getBitmap(requireActivity().contentResolver, uri)
                imagePathUriString = getPath(uri!!)
            } catch (e: IOException) {
                Timber.e("Unable to upload file from the given uri: $e")
                e.printStackTrace()
            }
        }

        if (resultCode == RESULT_OK && requestCode == RC_PICK_VIDEO) {

            val uri = data?.data
            try {
                uri?.let {
                    selectedVideoPath = getPath(it)
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor =
                        context?.contentResolver?.query(uri, filePathColumn, null, null, null)
                    cursor?.moveToFirst()

                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])

                    val picturePath = cursor?.getString(columnIndex ?: 0)
                    cursor?.close()

                    videoThumb =
                        ThumbnailUtils.createVideoThumbnail(
                            picturePath.toString(),
                            MediaStore.Video.Thumbnails.MICRO_KIND
                        )
                }
                if (selectedVideoPath == null) {
                    Timber.e("selected video path = null!")

                } else {
                    /**
                     * try to do something there
                     * selectedVideoPath is path to the selected video
                     */
                }
            } catch (e: IOException) {
                e.printStackTrace()

            }
        }
    }

    private fun getPath(uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (needToCheckUri && DocumentsContract.isDocumentUri(
                requireContext().applicationContext,
                uri
            )
        ) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    when (type) {
                        "image" -> {
                            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
        }
        if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = requireActivity().contentResolver
                    .query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }

        } else if ("file".equals(uri.scheme!!, ignoreCase = true))
            return uri.path

        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    open fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            Images.Media.insertImage(requireContext().contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    open fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (requireActivity().contentResolver != null) {
            val cursor: Cursor =
                requireActivity().contentResolver.query(uri!!, null, null, null, null)!!
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(Images.ImageColumns.DATA)
            path = cursor.getString(idx)
            cursor.close()
        }
        return path
    }

}