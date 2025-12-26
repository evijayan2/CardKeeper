package com.vijay.cardkeeper.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

/**
 * Full-screen barcode scanner using CameraX with real-time ML Kit barcode detection. Optimized for
 * PDF417 barcodes on driver licenses. Returns both the barcode data and a captured image.
 */
@Composable
fun BarcodeScannerScreen(
        onBarcodeScanned: (barcodeData: String, capturedImage: Bitmap?) -> Unit,
        onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted
                ->
                hasCameraPermission = granted
            }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        // Show permission request UI
        Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Text(
                    "Camera permission is required to scan barcodes",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDismiss) { Text("Cancel") }
        }
        return
    }

    // Barcode scanner setup
    val barcodeScanner = remember {
        val options =
                BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_PDF417,
                                Barcode.FORMAT_DATA_MATRIX,
                                Barcode.FORMAT_QR_CODE
                        )
                        .build()
        BarcodeScanning.getClient(options)
    }

    var isScanning by remember { mutableStateOf(true) }

    DisposableEffect(Unit) { onDispose { barcodeScanner.close() } }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
                factory = { ctx ->
                    val previewView =
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val executor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener(
                            {
                                val cameraProvider = cameraProviderFuture.get()

                                val preview =
                                        Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }

                                val imageAnalysis =
                                        ImageAnalysis.Builder()
                                                .setTargetResolution(Size(1280, 720))
                                                .setBackpressureStrategy(
                                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                                )
                                                .build()

                                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                    if (!isScanning) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    @androidx.camera.core.ExperimentalGetImage
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val inputImage =
                                                InputImage.fromMediaImage(
                                                        mediaImage,
                                                        imageProxy.imageInfo.rotationDegrees
                                                )

                                        barcodeScanner
                                                .process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        barcode.rawValue?.let { rawValue ->
                                                            if (rawValue.isNotEmpty() && isScanning
                                                            ) {
                                                                isScanning = false
                                                                Log.d(
                                                                        "BarcodeScannerScreen",
                                                                        "Barcode found: ${rawValue.take(50)}..."
                                                                )

                                                                // Capture the current frame as
                                                                // bitmap
                                                                val capturedBitmap =
                                                                        imageProxyToBitmap(
                                                                                imageProxy
                                                                        )

                                                                onBarcodeScanned(
                                                                        rawValue,
                                                                        capturedBitmap
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                            "BarcodeScannerScreen",
                                                            "Barcode scan failed",
                                                            e
                                                    )
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                    } else {
                                        imageProxy.close()
                                    }
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                    )
                                } catch (e: Exception) {
                                    Log.e("BarcodeScannerScreen", "Camera binding failed", e)
                                }
                            },
                            ContextCompat.getMainExecutor(ctx)
                    )

                    previewView
                },
                modifier = Modifier.fillMaxSize()
        )

        // Scan area overlay
        Box(
                modifier =
                        Modifier.align(Alignment.Center)
                                .size(300.dp, 150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
        )

        // Instructions
        Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    "Position the barcode within the frame",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    "Scanning for PDF417 barcode...",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        // Close button
        IconButton(
                onClick = onDismiss,
                modifier =
                        Modifier.align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White) }
    }
}

/** Converts an ImageProxy to a Bitmap for saving, cropped to center card area. */
@androidx.camera.core.ExperimentalGetImage
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val image = imageProxy.image ?: return null

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val imageBytes = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // Rotate bitmap based on rotation degrees
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        // Crop to center area matching driver license aspect ratio (credit card: 85.6mm × 53.98mm ≈
        // 1.586:1)
        // Take about 60% of the image width centered, with card aspect ratio
        val cardAspectRatio = 1.586f
        val cropWidthPercent = 0.65f // Take 65% of width

        val cropWidth = (bitmap.width * cropWidthPercent).toInt()
        val cropHeight = (cropWidth / cardAspectRatio).toInt()

        // Center the crop
        val cropX = (bitmap.width - cropWidth) / 2
        val cropY = (bitmap.height - cropHeight) / 2

        // Make sure crop is within bounds
        if (cropX >= 0 &&
                        cropY >= 0 &&
                        cropX + cropWidth <= bitmap.width &&
                        cropY + cropHeight <= bitmap.height
        ) {
            Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
        } else {
            bitmap // Return uncropped if crop is out of bounds
        }
    } catch (e: Exception) {
        Log.e("BarcodeScannerScreen", "Failed to convert image to bitmap", e)
        null
    }
}
