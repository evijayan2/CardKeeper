package com.vijay.cardkeeper.ui.item

import com.google.common.truth.Truth.assertThat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import org.junit.jupiter.api.Test

class ViewItemScreenKtTest {

    @Test
    @org.junit.jupiter.api.Disabled("Requires Android Runtime (Bitmap)")
    fun `generateBarcodeBitmap should return a bitmap for valid input`() {
        val bitmap = generateBarcodeBitmap("123456", Barcode.FORMAT_CODE_128)
        assertThat(bitmap).isNotNull()
    }

    @Test
    @org.junit.jupiter.api.Disabled("Requires Android Runtime (Bitmap)")
    fun `generateBarcodeBitmap should return null for invalid input`() {
        val bitmap = generateBarcodeBitmap("", Barcode.FORMAT_CODE_128)
        assertThat(bitmap).isNull()
    }

    @Test
    fun `mapToZXingFormat should return correct format`() {
        assertThat(mapToZXingFormat(Barcode.FORMAT_QR_CODE)).isEqualTo(BarcodeFormat.QR_CODE)
        assertThat(mapToZXingFormat(Barcode.FORMAT_UPC_A)).isEqualTo(BarcodeFormat.UPC_A)
        assertThat(mapToZXingFormat(Barcode.FORMAT_UPC_E)).isEqualTo(BarcodeFormat.UPC_E)
        assertThat(mapToZXingFormat(Barcode.FORMAT_EAN_13)).isEqualTo(BarcodeFormat.EAN_13)
        assertThat(mapToZXingFormat(Barcode.FORMAT_EAN_8)).isEqualTo(BarcodeFormat.EAN_8)
        assertThat(mapToZXingFormat(Barcode.FORMAT_CODE_128)).isEqualTo(BarcodeFormat.CODE_128)
        assertThat(mapToZXingFormat(Barcode.FORMAT_CODE_39)).isEqualTo(BarcodeFormat.CODE_39)
        assertThat(mapToZXingFormat(Barcode.FORMAT_CODE_93)).isEqualTo(BarcodeFormat.CODE_93)
        assertThat(mapToZXingFormat(Barcode.FORMAT_CODABAR)).isEqualTo(BarcodeFormat.CODABAR)
        assertThat(mapToZXingFormat(Barcode.FORMAT_DATA_MATRIX)).isEqualTo(BarcodeFormat.DATA_MATRIX)
        assertThat(mapToZXingFormat(Barcode.FORMAT_ITF)).isEqualTo(BarcodeFormat.ITF)
        assertThat(mapToZXingFormat(Barcode.FORMAT_PDF417)).isEqualTo(BarcodeFormat.PDF_417)
        assertThat(mapToZXingFormat(Barcode.FORMAT_AZTEC)).isEqualTo(BarcodeFormat.AZTEC)
        assertThat(mapToZXingFormat(Barcode.FORMAT_UNKNOWN)).isEqualTo(BarcodeFormat.CODE_128)
    }
}
