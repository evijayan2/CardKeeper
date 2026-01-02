package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vijay.cardkeeper.data.entity.PanCard
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Scanner for PAN (Permanent Account Number) cards using OCR.
 * Extracts PAN number, name, father's name, and DOB from card images.
 * 
 * PAN Format: ABCDE1234F (5 letters + 4 digits + 1 letter)
 */
class PanCardScanner {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // PAN number regex: 5 uppercase letters + 4 digits + 1 uppercase letter
    private val panPattern = Regex("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b")
    
    // Date pattern for DOB
    private val datePattern = Regex(
        "\\b(0[1-9]|[12][0-9]|3[01])[/-](0[1-9]|1[0-2])[/-](\\d{4})\\b|" +
        "\\b(0[1-9]|1[0-2])[/-](0[1-9]|[12][0-9]|3[01])[/-](\\d{4})\\b"
    )

    suspend fun scan(bitmap: Bitmap, isBackSide: Boolean = false): PanCardScanResult =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d("PanCardScanner", "Raw OCR Text: $text")

                    var panNumber = ""
                    var holderName = ""
                    var fatherName = ""
                    var dob = ""

                    // Extract PAN number
                    panPattern.find(text)?.let { match ->
                        panNumber = match.value
                        Log.d("PanCardScanner", "Found PAN: $panNumber")
                    }

                    // Extract DOB
                    datePattern.find(text)?.let { match ->
                        dob = match.value
                        Log.d("PanCardScanner", "Found DOB: $dob")
                    }

                    // Extract names (more complex heuristic)
                    val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text }
                    
                    for (i in lines.indices) {
                        val line = lines[i].uppercase()
                        
                        // Look for "Name" field
                        if (line.contains("NAME") && i + 1 < lines.size) {
                            if (holderName.isEmpty() && !line.contains("FATHER")) {
                                // Next line is likely the holder name
                                holderName = lines[i + 1].trim()
                                Log.d("PanCardScanner", "Found Holder Name: $holderName")
                            }
                        }
                        
                        // Look for "Father's Name" field
                        if ((line.contains("FATHER") || line.contains("FATHER'S NAME")) && 
                            i + 1 < lines.size) {
                            fatherName = lines[i + 1].trim()
                            Log.d("PanCardScanner", "Found Father Name: $fatherName")
                        }
                    }

                    // Alternative: Look for capitalized names not containing keywords
                    if (holderName.isEmpty()) {
                        for (line in lines) {
                            if (line.matches(Regex("^[A-Z ]{5,}$")) && 
                                line.length > 5 &&
                                !line.contains("INCOME") &&
                                !line.contains("TAX") &&
                                !line.contains("DEPARTMENT") &&
                                !line.contains("INDIA") &&
                                !line.contains("GOVERNMENT") &&
                                !line.contains("PERMANENT") &&
                                !line.contains("ACCOUNT") &&
                                panNumber.isNotEmpty() && 
                                !line.contains(panNumber)) {
                                holderName = line.trim()
                                Log.d("PanCardScanner", "Found Name (heuristic): $holderName")
                                break
                            }
                        }
                    }

                    val result = PanCardScanResult(
                        panNumber = panNumber,
                        holderName = holderName,
                        fatherName = fatherName,
                        dob = dob,
                        rawText = text
                    )

                    Log.d("PanCardScanner", "Scan Result: $result")
                    if (cont.isActive) {
                        cont.resume(result)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PanCardScanner", "OCR failed", e)
                    if (cont.isActive) {
                        cont.resume(PanCardScanResult())
                    }
                }
        }
}

data class PanCardScanResult(
    val panNumber: String = "",
    val holderName: String = "",
    val fatherName: String = "",
    val dob: String = "",
    val rawText: String = ""
)
