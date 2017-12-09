package me.ialistannen.vocabcardfrompicture

import me.ialistannen.vocabcardfrompicture.ocr.NativeTesseractOcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.OcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.columns.ColumnFinder
import me.ialistannen.vocabcardfrompicture.ocr.sanitizer.SimpleGermanReplacementSanitizer
import me.ialistannen.vocabcardfrompicture.ocr.toBufferedImage
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    System.load("/usr/share/opencv/java/libopencv_java331.so")

    val readImage = Imgcodecs.imread(
            "/tmp/lat/Unmodified_image.jpg", Imgcodecs.IMREAD_GRAYSCALE
    )

    val ocrProvider: OcrProvider = NativeTesseractOcrProvider("deu")

    for ((counter, columnImage) in ColumnFinder().findColumns(readImage).withIndex()) {
        ImageIO.write(
                columnImage.toBufferedImage(),
                "png",
                File("/tmp/Test-$counter.png")
        )
        ocrProvider.performOcr(columnImage.toBufferedImage())
                .subscribe(
                        {
                            val string = SimpleGermanReplacementSanitizer().sanitize(it)
                            println("Got\n$string")
                        },
                        { it.printStackTrace() }
                )
    }
}