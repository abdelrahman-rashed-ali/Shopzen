package shopzen.presentation.search.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {
    // The server is throwing 413 Payload Too Large, which typically indicates a 1MB or 2MB Nginx default limit.
    private const val MAX_SIZE_BYTES = 1 * 1024 * 1024 // 1 MB

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun compressImageFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return null

            var quality = 100
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            while (stream.size() > MAX_SIZE_BYTES && quality > 10) {
                stream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            // If still too large, downscale the bitmap
            var currentBitmap = bitmap
            while (stream.size() > MAX_SIZE_BYTES) {
                stream.reset()
                val newWidth = (currentBitmap.width * 0.8).toInt()
                val newHeight = (currentBitmap.height * 0.8).toInt()
                if (newWidth <= 0 || newHeight <= 0) break
                val scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, max(1, newWidth), max(1, newHeight), true)
                if (currentBitmap != bitmap) {
                    currentBitmap.recycle()
                }
                currentBitmap = scaledBitmap
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
