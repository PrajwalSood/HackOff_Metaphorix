package com.shivam.emotions.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import com.shivam.emotions.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class BitmapUtils(private val context: Context) {
    fun compressImage(image: Bitmap?): Bitmap? {
        val baos = ByteArrayOutputStream()
        image!!.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) //Compression quality, here 100 means no compression, the storage of compressed data to baos
        var options = 90
        while (baos.toByteArray().size / 1024 > 400) {  //Loop if compressed picture is greater than 400kb, than to compression
            baos.reset() //Reset baos is empty baos
            image.compress(
                Bitmap.CompressFormat.JPEG,
                options,
                baos
            ) //The compression options%, storing the compressed data to the baos
            options -= 10 //Every time reduced by 10
        }
        val isBm =
            ByteArrayInputStream(baos.toByteArray()) //The storage of compressed data in the baos to ByteArrayInputStream
        return BitmapFactory.decodeStream(isBm, null, null)
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        // Determine the constrained dimension, which determines both dimensions.
        val width: Int
        val height: Int
        val widthRatio = bitmap.width.toFloat() / maxWidth
        val heightRatio = bitmap.height.toFloat() / maxHeight
        // Width constrained.
        if (widthRatio >= heightRatio) {
            width = maxWidth
            height = (width.toFloat() / bitmap.width * bitmap.height).toInt()
        } else {
            height = maxHeight
            width = (height.toFloat() / bitmap.height * bitmap.width).toInt()
        }
        val scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val ratioX = width.toFloat() / bitmap.width
        val ratioY = height.toFloat() / bitmap.height
        val middleX = width / 2.0f
        val middleY = height / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bitmap,
            middleX - bitmap.width / 2,
            middleY - bitmap.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        return scaledBitmap
    }

    fun preparePrajuNetBitmap(bmpOriginal: Bitmap): Bitmap {
        return toGrayScaleBitmap(
            resizeBitmap(
                bmpOriginal
            )
        )
    }

    private fun toGrayScaleBitmap(bmpOriginal: Bitmap): Bitmap {
        val bmpGrayscale =
            Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun resizeBitmap(bmpOriginal: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(
            bmpOriginal,
            48,
            48,
            true
        )
    }

    fun getStraightBitmap(
        bmpOriginal: Bitmap,
        bmpPath: String,
        isUri: Boolean,
        activity: FragmentActivity
    ): Bitmap {
        val exifInterface: ExifInterface
        exifInterface = if (isUri) {
            val gg = activity.contentResolver.openInputStream(Uri.parse(bmpPath))
            ExifInterface(gg!!)
        } else {
            ExifInterface(bmpPath)
        }
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bmpOriginal, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bmpOriginal, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bmpOriginal, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bmpOriginal
            else -> bmpOriginal
        }
    }

    fun drawRectangle(bmpOriginal: Bitmap, rect: RectF): Bitmap {
        val bitmap = bmpOriginal.copy(bmpOriginal.config, true)
        val canvas = Canvas(bitmap)

        Paint().apply {
            color = ContextCompat.getColor(context, R.color.orangeFFA500)
            isAntiAlias = true
            strokeWidth = 10f
            style = Paint.Style.STROKE
            // draw rectangle on canvas
            canvas.drawRect(
                rect.left, // left side of the rectangle to be drawn
                rect.top, // top side
                rect.right, // right side
                rect.bottom, // bottom side
                this
            )
        }

        return bitmap
    }
}