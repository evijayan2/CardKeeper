package com.vijay.cardkeeper.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Input is expected to be raw digits
        val trim = if (text.text.length >= 8) text.text.substring(0, 8) else text.text
        
        val out = StringBuilder()
        for (i in trim.indices) {
            out.append(trim[i])
            if (i == 1 && trim.length > 2) out.append("/")
            if (i == 3 && trim.length > 4) out.append("/")
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val result = if (offset <= 1) offset
                else if (offset <= 3) offset + 1
                else if (offset <= 8) offset + 2
                else 10
                return result.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val result = if (offset <= 2) offset
                else if (offset <= 5) offset - 1
                else if (offset <= 10) offset - 2
                else 8
                return result.coerceAtMost(trim.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), numberOffsetTranslator)
    }
}
