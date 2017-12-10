package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect

/**
 * Extracts the **two** columns based on the passed column starts and contours.
 */
internal class ColumnImageExtractor {

    companion object {
        private val STANDARD_DERIVATION = 100
    }

    fun extract(contours: List<MatOfPoint>,
                leftColumnStart: Int,
                rightColumnStart: Int,
                image: Mat): List<Mat> {
        return listOf(
                getColumnImage(contours.inColumn(leftColumnStart), image),
                getColumnImage(contours.inColumn(rightColumnStart), image)
        )
    }

    private fun getColumnImage(contours: List<MatOfPoint>, image: Mat): Mat {
        return Mat(image, Rect(
                Point(contours.findXStartOfColumn(), contours.findYStartOfColumn()),
                Point(contours.findXEndOfColumn(), contours.findYEndOfColumn())
        ))
    }

    private fun List<MatOfPoint>.inColumn(start: Int): List<MatOfPoint> {
        return filter {
            val min = it.toList().map { it.x }.min()!!

            min <= start + STANDARD_DERIVATION && min >= start - STANDARD_DERIVATION
        }
    }

    private fun List<MatOfPoint>.findYStartOfColumn(): Double {
        return flatMap { it.toList() }
                .map { it.y }
                .min()!!
    }

    private fun List<MatOfPoint>.findYEndOfColumn(): Double {
        return flatMap { it.toList() }
                .map { it.y }
                .max()!!
    }

    private fun List<MatOfPoint>.findXStartOfColumn(): Double {
        return flatMap { it.toList() }
                .map { it.x }
                .min()!!
    }

    private fun List<MatOfPoint>.findXEndOfColumn(): Double {
        return flatMap { it.toList() }
                .map { it.x }
                .max()!!
    }
}