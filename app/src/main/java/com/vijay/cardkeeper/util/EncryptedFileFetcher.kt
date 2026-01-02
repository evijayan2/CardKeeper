package com.vijay.cardkeeper.util

import coil3.ImageLoader
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.decode.ImageSource
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * Identifies .enc files and provides them as a Source with a custom MIME type.
 * The actual decryption and decoding is handled by EncryptedImageDecoder.
 */
class EncryptedFileFetcher(
    private val file: File,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return SourceFetchResult(
            source = ImageSource(file = file.toOkioPath(), fileSystem = FileSystem.SYSTEM),
            mimeType = "application/x-encrypted-cardkeeper",
            dataSource = coil3.decode.DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<Any> {
        override fun create(
            data: Any,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            val file = when (data) {
                is File -> data
                is String -> File(data)
                is android.net.Uri -> data.path?.let { File(it) }
                else -> null
            }

            if (file != null && file.extension.equals("enc", ignoreCase = true)) {
                return EncryptedFileFetcher(file, options)
            }
            return null
        }
    }
}
