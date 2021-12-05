package com.example.SGmono.service

import com.example.SGmono.properties.FileStorageProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.URL
import java.nio.channels.Channels


@Service
class FileStorageService(
    private val env: Environment,
    private val fileStorageProperties: FileStorageProperties
) {
    fun downloadFile(fileURL: String, fileName: String) {
        val url = URL(fileURL)
        Channels.newChannel(url.openStream()).use { readableByteChannel ->
            FileOutputStream(fileName).use { fileOutputStream ->
                fileOutputStream.channel.use { fileChannel ->
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
                    fileOutputStream.close()
                }
            }
        }
    }

    fun writeToFile(data: String, fileName: String) {
        BufferedWriter(
            FileWriter(fileName)
        ).let {
            it.write(data)
            it.close()
        }
    }
}