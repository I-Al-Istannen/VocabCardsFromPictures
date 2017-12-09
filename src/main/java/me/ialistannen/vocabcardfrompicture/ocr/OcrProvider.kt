package me.ialistannen.vocabcardfrompicture.ocr

import io.reactivex.Single
import java.awt.image.BufferedImage

/**
 * The base for a service that performs OCR.
 */
interface OcrProvider {

    /**
     * @param input The input image
     * @return An observable that emits a single string when it is done.
     */
    fun performOcr(input: BufferedImage): Single<String>
}