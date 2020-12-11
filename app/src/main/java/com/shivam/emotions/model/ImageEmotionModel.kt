package com.shivam.emotions.model

import android.graphics.Bitmap

data class ImageEmotionModel(
    val id: Int,
    val imageBitmap: Bitmap,
    val emotion: String?,
    val compoundEmotion: String?
)