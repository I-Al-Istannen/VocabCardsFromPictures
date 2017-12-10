package me.ialistannen.vocabcardfrompicture

import io.reactivex.Flowable
import me.ialistannen.vocabcardfrompicture.ocr.NativeTesseractOcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.OcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.columns.ColumnFinder
import me.ialistannen.vocabcardfrompicture.ocr.sanitizer.SimpleGermanReplacementSanitizer
import me.ialistannen.vocabcardfrompicture.ocr.toBufferedImage
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    System.load("/usr/share/opencv/java/libopencv_java331.so")

    val ocrProvider: OcrProvider = NativeTesseractOcrProvider("deu")

    var ocrResults: Flowable<String> = Flowable.empty()
//    ocrResults = ocrImage(
//            Imgcodecs.imread("/tmp/lat/Unmodified_image.jpg", Imgcodecs.IMREAD_GRAYSCALE),
//            ocrResults,
//            ocrProvider
//    )
    ocrResults = ocrImage(
            Imgcodecs.imread("/tmp/lat/Unmodified_image_german.jpg", Imgcodecs.IMREAD_GRAYSCALE),
            ocrResults,
            ocrProvider
    )

    ocrResults
            .map {
                SimpleGermanReplacementSanitizer().sanitize(
                        it.lines().filter { it.isNotBlank() }.joinToString("\n")
                )
            }
            .buffer(2) // two columns
            .subscribe(
                    {
                        println("Got: $it\n\n")
                    },
                    { it.printStackTrace() }
            )

}

private fun ocrImage(readImage: Mat,
                     ocrResults: Flowable<String>,
                     ocrProvider: OcrProvider): Flowable<String> {
    var resultFlowable = ocrResults
    for ((counter, columnImage) in ColumnFinder().findColumns(readImage).withIndex()) {
        ImageIO.write(
                columnImage.toBufferedImage(),
                "png",
                File("/tmp/Test-$counter.png")
        )
        resultFlowable = resultFlowable.concatWith(
                ocrProvider.performOcr(columnImage.toBufferedImage()).toFlowable()
        )
    }
    return resultFlowable
}