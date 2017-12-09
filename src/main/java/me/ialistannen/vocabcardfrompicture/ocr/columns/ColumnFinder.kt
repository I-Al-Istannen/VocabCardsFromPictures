package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


/**
 * Finds the columns in the image and returns their images.
 */
class ColumnFinder {

    /**
     * Attempts to find the two columns and returns images for them.
     *
     * @return The found images
     */
    fun findColumns(inputImage: Mat): List<Mat> {
        val enhancedImage = enhanceImage(inputImage)
        val targetMat = sanitizeImage(enhancedImage)

        val contours = findContoursWithGreaterWidth(targetMat, 200)
        val columnStarts = findColumnStarts(contours)

        val columnImages = getColumnImages(columnStarts, enhancedImage)

        enhancedImage.release()
        targetMat.release()

        return columnImages
    }

    private fun sanitizeImage(targetMat: Mat): Mat {
        val image = targetMat.clone()

        // dilation and then eroding removes small artifacts
        Imgproc.dilate(image, image, Imgproc.getStructuringElement(
                Imgproc.MORPH_DILATE,
                Size(6.0, 6.0)
        ))


        val kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_ERODE,
                Size(50.0, 50.0)
        )
        Imgproc.erode(image, image, kernel)

        return image
    }

    private fun enhanceImage(sourceImage: Mat): Mat {
        val image = sourceImage.clone()
        // adaptive binarization => reduce noise
        Imgproc.adaptiveThreshold(
                image,
                image,
                255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                15,
                15.0
        )

        return image
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

    private fun findColumnStarts(contours: List<MatOfPoint>): List<Int> {
        val sortedContours = contours.sortedBy { it.toList().map { it.x }.min()!! }
        var lastMinX: Double = -1.0
        val mins = sortedSetOf<Double>()

        for (contour in sortedContours) {
            val minX = contour.toList().map { it.x }.min()!!

            if (lastMinX < 0) {
                lastMinX = minX
                mins.add(lastMinX)
            }


            if (minX !in lastMinX - 100..lastMinX + 100) {
                mins.add(minX)
                lastMinX = minX
            }
        }

        return listOf(mins.first().toInt(), mins.last().toInt())
    }

    private fun getColumnImages(columnStarts: List<Int>, image: Mat): List<Mat> {
        val firstColumnStart = columnStarts.first()
        val secondColumnStart = columnStarts.last()
        val widthOfFirstColumn = secondColumnStart - firstColumnStart
        val widthOfSecondColumn = image.width() - secondColumnStart

        val copiedLeft = Mat(image, Rect(
                firstColumnStart, 0,
                widthOfFirstColumn, image.height()
        ))
                .apply { makeTextMoreReadable(this) }

        val copiedRight = Mat(image, Rect(
                secondColumnStart, 0,
                widthOfSecondColumn, image.height()
        ))
                .apply { makeTextMoreReadable(this) }

        return listOf(copiedLeft, copiedRight)
    }

    private fun makeTextMoreReadable(image: Mat) {
        // dilation and then eroding makes the text a bit bigger, to smooth out imperfections
        val kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_ERODE,
                Size(2.0, 2.0)
        )
        Imgproc.erode(image, image, kernel)

        // and blur it, to remove harsh edges and holes in letters
        Imgproc.blur(image, image, Size(5.0, 5.0))
    }
}