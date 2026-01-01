package com.vijay.cardkeeper.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream


fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    var newWidth = originalWidth
    var newHeight = originalHeight

    if (originalWidth > maxDimension || originalHeight > maxDimension) {
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        if (originalWidth > originalHeight) {
            newWidth = maxDimension
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * aspectRatio).toInt()
        }
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, name: String): String {
    val directory = context.getDir("card_images", Context.MODE_PRIVATE)
    val file = File(directory, "$name.jpg")
    // Resize to max 1920px (Full HD) to balance quality and memory
    val resizedBitmap = resizeBitmap(bitmap, 1280) 
    FileOutputStream(file).use { stream -> resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream) }
    return file.absolutePath
}
