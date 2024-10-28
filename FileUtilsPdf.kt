package com.ledure.galaxy.helper

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.*

class FileUtilsPdf {

    companion object {
        fun getPathFromUri(context: Context, uri: Uri): String? {
            return if ("content".equals(uri.scheme, ignoreCase = true)) {
                getContentFilePath(context, uri)
            } else {
                null
            }
        }

        @SuppressLint("Range")
        private fun getContentFilePath(context: Context, uri: Uri): String? {
            var path: String? = null
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    if (displayName != null) {
                        path = if (displayName.startsWith("raw:") || displayName.startsWith("msf:")) {
                            displayName.substring(4)
                        } else {
                            copyFileToCache(context, uri, displayName)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return path
        }

        private fun copyFileToCache(context: Context, uri: Uri, displayName: String): String? {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            return try {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val cacheDir = context.cacheDir
                    val tempFile = File(cacheDir, displayName)
                    outputStream = FileOutputStream(tempFile)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                    tempFile.absolutePath
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
