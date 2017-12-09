package me.ialistannen.vocabcardfrompicture

import me.ialistannen.vocabcardfrompicture.ocr.NativeTesseractOcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.OcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.sanitizer.SimpleGermanReplacementSanitizer
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val ocrProvider: OcrProvider = NativeTesseractOcrProvider("deu")

    val image = ImageIO.read(File("/tmp/ocr_test_deu.png"))

    ocrProvider.performOcr(image)
            .subscribe(
                    {
                        val string = SimpleGermanReplacementSanitizer().sanitize(it)
                        println("Got\n$string")
                    },
                    { it.printStackTrace() }
            )
}