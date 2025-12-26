package com.vijay.cardkeeper.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, name: String): String {
    val directory = context.getDir("card_images", Context.MODE_PRIVATE)
    val file = File(directory, "$name.jpg")
    FileOutputStream(file).use { stream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream) }
    return file.absolutePath
}
