package me.ialistannen.vocabcardfrompicture

import io.reactivex.Observable
import me.ialistannen.vocabcardfrompicture.cards.VocabCard
import me.ialistannen.vocabcardfrompicture.ocr.NativeTesseractOcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.OcrProvider
import me.ialistannen.vocabcardfrompicture.ocr.columns.ColumnFinder
import me.ialistannen.vocabcardfrompicture.ocr.sanitizer.OcrResultSanitizer
import me.ialistannen.vocabcardfrompicture.ocr.sanitizer.SimpleGermanReplacementSanitizer
import me.ialistannen.vocabcardfrompicture.ocr.toBufferedImage
import me.ialistannen.vocabcardfrompicture.parsing.StringToCardParser
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

/**
 * Creates cards based on images.
 */
class CardReader(private val ocrProvider: OcrProvider = NativeTesseractOcrProvider("deu"),
                 private val ocrSanitizer: OcrResultSanitizer = SimpleGermanReplacementSanitizer(),
                 private val columnFinder: ColumnFinder = ColumnFinder(),
                 private val stringToCardParser: StringToCardParser = StringToCardParser()) {

    companion object {
        private val COLUMN_COUNT = 2
    }

    /**
     * Reads cards from the provided image paths.
     * May throw an error if the image is not readable
     */
    fun readCards(germanImage: File, latinImage: File): Observable<List<VocabCard>> {
        return readCards(
                Imgcodecs.imread(germanImage.absolutePath, Imgcodecs.IMREAD_GRAYSCALE),
                Imgcodecs.imread(latinImage.absolutePath, Imgcodecs.IMREAD_GRAYSCALE)
        )
    }

    /**
     * Reads cards from the provided images.
     */
    fun readCards(imageGerman: Mat, imageLatin: Mat): Observable<List<VocabCard>> {
        var observable = Observable.empty<String>()
        observable = ocrImage(imageGerman, observable)
        observable = ocrImage(imageLatin, observable)

        return observable.map {
            ocrSanitizer.sanitize(
                    it.lines().filter { it.isNotBlank() }.joinToString("\n")
            )
        }
                .buffer(COLUMN_COUNT)
                .map { it.joinToString("\n") }
                .buffer(2)
                .map {
                    stringToCardParser.parse(
                            it[0].lines(),
                            it[1].lines()
                    )
                }
                .cache()
    }

    private fun ocrImage(image: Mat, observable: Observable<String>): Observable<String> {
        var newObservable = observable
        for (column in columnFinder.findColumns(image)) {
            newObservable = newObservable.concatWith(
                    ocrProvider.performOcr(column.toBufferedImage()).toObservable()
            )
        }
        return newObservable
    }
}