package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


private val standardDerivation = 100

/**
 * Finds the columns in the image and returns their images.
 */
class ColumnFinder {

    private val imageSanitizer: ImageSanitizer = ImageSanitizer()

    /**
     * Attempts to find the two columns and returns images for them.
     *
     * @return The found images
     */
    fun findColumns(inputImage: Mat): List<Mat> {
        val enhancedImage = imageSanitizer.enhance(inputImage)
        val imageForContourDetection = imageSanitizer.sanitizeForContourDetection(enhancedImage)

        Imgcodecs.imwrite("/tmp/hm.png", imageForContourDetection)
        Imgcodecs.imwrite("/tmp/hm_en.png", enhancedImage)

        val contours = findContoursWithGreaterWidth(imageForContourDetection, 200)
        val columnXStarts = findColumnXStarts(contours)

        val mat = imageForContourDetection.clone()
        for ((index, _) in contours.withIndex()) {
            Imgproc.drawContours(
                    mat,
                    contours,
                    index,
                    Scalar.all(255.0)
            )
        }

        for (columnStart in columnXStarts) {
            Imgproc.line(
                    mat,
                    Point(columnStart.toDouble(), 0.0),
                    Point(columnStart.toDouble(), mat.height().toDouble()),
                    Scalar.all(255.0),
                    4
            )
        }
        Imgcodecs.imwrite("/tmp/hm2.png", mat)

        val columnImages = ColumnImageExtractor(standardDerivation).extract(
                contours,
                columnXStarts.first(),
                columnXStarts.last(),
                enhancedImage
        )
        enhancedImage.release()
        imageForContourDetection.release()

        return columnImages
    }

    private fun findContoursWithGreaterWidth(targetMat: Mat, minWidth: Int): List<MatOfPoint> {
        val contours = arrayListOf<MatOfPoint>()

        Imgproc.Canny(
                targetMat, targetMat, 100.0, 100.0
        )

        Imgproc.findContours(
                targetMat, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE
        )
        return contours.filter {
            val points = it.toList().map { it.x }
            points.max()!! - points.min()!! >= minWidth
        }
    }

    private fun findColumnXStarts(contours: List<MatOfPoint>): List<Int> {
        val sortedContours = contours.sortedBy { it.toList().map { it.x }.min()!! }
        var lastMinX: Double = -1.0
        val mins = sortedSetOf<Double>()

        for (contour in sortedContours) {
            val minX = contour.toList().map { it.x }.min()!!

            if (lastMinX < 0) {
                lastMinX = minX
                mins.add(lastMinX)
            }


            if (minX !in lastMinX - standardDerivation..lastMinX + standardDerivation) {
                mins.add(minX)
                lastMinX = minX
            }
        }

        return listOf(mins.first().toInt(), mins.last().toInt())
    }
}