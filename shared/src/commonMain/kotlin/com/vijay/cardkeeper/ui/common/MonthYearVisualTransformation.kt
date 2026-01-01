package com.vijay.cardkeeper.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MonthYearVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Input is expected to be raw digits (MMYY)
        val trim = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
        
        val out = StringBuilder()
        for (i in trim.indices) {
            out.append(trim[i])
            // Only append slash if we are after the month (index 1) AND there is another digit coming
            if (i == 1 && trim.length > 2) out.append("/")
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val result = if (offset <= 1) offset
                else offset + 1
                return result.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val result = if (offset <= 2) offset
                else offset - 1
                return result.coerceAtMost(trim.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), numberOffsetTranslator)
    }
}
