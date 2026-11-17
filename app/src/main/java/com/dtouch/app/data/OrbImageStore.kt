package com.dtouch.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Guarda la imagen personalizada del orbe en el almacenamiento interno
 * de la app. No se usa almacenamiento externo ni permisos de lectura.
 *
 * El archivo se reescala a un máximo de 512 px y se guarda como PNG para
 * conservar transparencia. La app detecta la imagen por la existencia del
 * archivo; no se guarda una ruta en SharedPreferences.
 */
class OrbImageStore(context: Context) {

    private val appContext = context.applicationContext
    private val imageFile = File(appContext.filesDir, IMAGE_FILE_NAME)
    private val tempFile = File(appContext.filesDir, "$IMAGE_FILE_NAME.tmp")

    fun hasCustomImage(): Boolean = imageFile.isFile && imageFile.length() > 0L

    fun loadBitmap(): Bitmap? {
        if (!hasCustomImage()) return null
        return BitmapFactory.decodeFile(imageFile.absolutePath) ?: run {
            imageFile.delete()
            null
        }
    }

    fun saveFromUri(uri: Uri): Boolean {
        return try {
            val resolver = appContext.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            // Con inJustDecodeBounds=true decodeStream devuelve null a propósito:
            // solo se rellenan outWidth/outHeight. No usar su resultado para validar.
            val boundsStream = resolver.openInputStream(uri) ?: return false
            boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                inJustDecodeBounds = false
            }

            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return false

            val scaled = scaleToFit(decoded, MAX_IMAGE_SIZE)
            if (scaled !== decoded) decoded.recycle()

            try {
                FileOutputStream(tempFile).use { output ->
                    if (!scaled.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        return false
                    }
                }
            } finally {
                scaled.recycle()
            }

            if (imageFile.exists() && !imageFile.delete()) return false
            if (!tempFile.renameTo(imageFile)) {
                tempFile.copyTo(imageFile, overwrite = true)
                tempFile.delete()
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo guardar la imagen del orbe", e)
            false
        } catch (e: OutOfMemoryError) {
            Log.w(TAG, "La imagen es demasiado grande para el orbe", e)
            false
        } finally {
            tempFile.delete()
        }
    }

    fun clear() {
        imageFile.delete()
        tempFile.delete()
    }

    private fun scaleToFit(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val largest = maxOf(width, height)
        if (largest <= maxSize) return bitmap

        val scale = maxSize.toFloat() / largest
        val newWidth = (width * scale).toInt().coerceAtLeast(1)
        val newHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        private const val TAG = "OrbImageStore"
        const val IMAGE_FILE_NAME = "orb_custom.png"
        const val MAX_IMAGE_SIZE = 512
    }
}
