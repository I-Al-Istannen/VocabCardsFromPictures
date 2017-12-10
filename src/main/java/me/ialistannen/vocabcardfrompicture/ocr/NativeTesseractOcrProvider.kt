package me.ialistannen.vocabcardfrompicture.ocr

import io.reactivex.Single
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * Uses a provided Tesseract installation to OCR it.
 */
class NativeTesseractOcrProvider(private val language: String) : OcrProvider {

    override fun performOcr(input: BufferedImage): Single<String> {
        return Single.create<String> {
            var tempFile: Path? = null
            try {
                tempFile = writeToTempFile(input)

                val processBuilder = ProcessBuilder(
                        "tesseract",
                        tempFile.toAbsolutePath().toString(),
                        "stdout",
                        "-l", language,
                        "-psm", "6"
                )

                println(processBuilder.command())

                val process = processBuilder.start()

                val readText = process.inputStream.bufferedReader().readText()

                process.waitFor(20, TimeUnit.SECONDS)
                if (process.exitValue() != 0) {
                    it.onError(RuntimeException("Exit code was non zero: '${process.exitValue()}'"))
                }

                it.onSuccess(readText)
            } catch (e: Exception) {
                it.onError(e)
            } finally {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile)
                }
            }
        }
    }

    private fun writeToTempFile(input: BufferedImage): Path {
        val tempFile = Files.createTempFile("vocab-cards", "image")
        ImageIO.write(input, "png", tempFile.toFile())

        return tempFile
    }
}