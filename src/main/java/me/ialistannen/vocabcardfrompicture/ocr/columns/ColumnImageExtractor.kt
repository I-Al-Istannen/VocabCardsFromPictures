package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Extracts the **two** columns based on the passed column starts and contours.
 */
internal class ColumnImageExtractor(private val standardDerivation: Int) {

    fun extract(contours: List<MatOfPoint>,
                leftColumnStart: Int,
                rightColumnStart: Int,
                image: Mat): List<Mat> {
        return listOf(
                getColumnImage(
                        contours.inColumn(leftColumnStart, rightColumnStart),
                        leftColumnStart, rightColumnStart,
                        image
                ),
                getColumnImage(
                        contours.inColumn(rightColumnStart, null),
                        rightColumnStart, null,
                        image
                )
        )
    }

    private fun getColumnImage(contours: List<MatOfPoint>,
                               columnStart: Int,
                               nextColumnStart: Int?,
                               image: Mat): Mat {
        return Mat(image, Rect(
                Point(
                        columnStart.toDouble(),
                        contours.findYStartOfColumn()
                ),
                Point(
                        nextColumnStart?.toDouble() ?: contours.findXEndOfColumn(),
                        contours.findYEndOfColumn()
                )
        )).makeTextMoreReadable()
    }

    private fun Mat.makeTextMoreReadable(): Mat {
        // dilation and then eroding makes the text a bit bigger, to smooth out imperfections
        val kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_ERODE,
                Size(2.0, 2.0)
        )
        Imgproc.erode(this, this, kernel)

        // and blur it, to remove harsh edges and holes in letters
        Imgproc.blur(this, this, Size(5.0, 5.0))

        return this
    }

    private fun List<MatOfPoint>.inColumn(start: Int, nextColumnStart: Int?): List<MatOfPoint> {
        return filter {
            val min = it.toList().map { it.x }.min()!!

            if (min < start - standardDerivation) {
                return@filter false
            }

            if (nextColumnStart == null) {
                return@filter true
            } else {
                val max = it.toList().map { it.x }.max()!!

                return@filter max <= nextColumnStart
            }
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

    private fun List<MatOfPoint>.findXEndOfColumn(): Double {
        return flatMap { it.toList() }
                .map { it.x }
                .max()!!
    }
}