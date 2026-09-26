package com.kanha.ide.build.tool

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class BuildToolsDownloader(private val context: Context) {

    suspend fun download(
        urlString: String,
        destFile: File,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedBps: Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
            }

            val fileLength = connection.contentLength.toLong()
            
            // Download the file
            connection.inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    var lastTime = System.currentTimeMillis()
                    var lastTotal: Long = 0
                    
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        output.write(data, 0, count)
                        
                        val currentTime = System.currentTimeMillis()
                        val timeDiff = currentTime - lastTime
                        if (timeDiff > 500) { // Update progress every 500ms
                            val speed = ((total - lastTotal) * 1000) / timeDiff
                            withContext(Dispatchers.Main) {
                                onProgress(total, fileLength, speed)
                            }
                            lastTime = currentTime
                            lastTotal = total
                        }
                    }
                    
                    // Final progress update
                    withContext(Dispatchers.Main) {
                        onProgress(total, fileLength, 0)
                    }
                }
            }
        } catch (e: Exception) {
            destFile.delete() // Clean up partial download
            throw e
        } finally {
            connection?.disconnect()
        }
    }
}
