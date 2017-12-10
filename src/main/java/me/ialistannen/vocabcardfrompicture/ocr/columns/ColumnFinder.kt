package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


private val standardDerivation = 100

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

        Imgcodecs.imwrite("/tmp/hm.png", targetMat)
        Imgcodecs.imwrite("/tmp/hm_en.png", enhancedImage)

        val contours = findContoursWithGreaterWidth(targetMat, 200)
        val columnXStarts = findColumnXStarts(contours)
        val columnStarts = columnXStarts.zip(
                columnXStarts.map { findContoursInColumn(contours, it).minY() }
        )
                .map { Point(it.first.toDouble(), it.second.toDouble()) }
        val columnYEnds = findColumnYEnds(contours, columnXStarts)

        val mat = targetMat.clone()
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
        for (columnHeight in columnYEnds) {
            Imgproc.line(
                    mat,
                    Point(0.0, columnHeight.toDouble()),
                    Point(mat.width().toDouble(), columnHeight.toDouble()),
                    Scalar.all(255.0),
                    4
            )
        }

        Imgcodecs.imwrite("/tmp/hm2.png", mat)

        val columnImages = getColumnImages(
                columnStarts,
                columnYEnds,
                findContoursInColumn(contours, columnXStarts[1]).maxX(),
                enhancedImage
        )

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
                Size(100.0, 100.0)
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

    private fun findColumnYEnds(contours: List<MatOfPoint>, columnStarts: List<Int>): List<Int> {
        return columnStarts.map { start -> findContoursInColumn(contours, start).maxY() }
    }

    private fun findContoursInColumn(contours: List<MatOfPoint>,
                                     columnStart: Int): List<MatOfPoint> {
        return contours.filter {
            val min = it.toList().map { it.x }.min()!!

            min <= columnStart + standardDerivation && min >= columnStart - standardDerivation
        }
    }

    private fun List<MatOfPoint>.maxY(): Int {
        return flatMap { it.toList() }.map { it.y }.max()!!.toInt()
    }

    private fun List<MatOfPoint>.minY(): Int {
        return flatMap { it.toList() }.map { it.y }.min()!!.toInt()
    }

    private fun List<MatOfPoint>.maxX(): Int {
        return flatMap { it.toList() }.map { it.x }.max()!!.toInt()
    }

    private fun getColumnImages(columnStarts: List<Point>,
                                columnHeights: List<Int>,
                                secondColumnEnd: Int,
                                image: Mat): List<Mat> {
        val firstColumnStartX = columnStarts.first().x.toInt()
        val secondColumnStartX = columnStarts.last().x.toInt()
        val widthOfFirstColumn = secondColumnStartX - firstColumnStartX
        val widthOfSecondColumn = secondColumnEnd - secondColumnStartX
        val firstColumnStartY = columnStarts.first().y.toInt()
        val secondColumnStartY = columnStarts.last().y.toInt()
        val heightOfFirstColumn = columnHeights[0] - firstColumnStartY
        val heightOfSecondColumn = columnHeights[1] - secondColumnStartY

        val copiedLeft = Mat(image, Rect(
                firstColumnStartX, firstColumnStartY,
                widthOfFirstColumn, heightOfFirstColumn
        ))
                .apply { makeTextMoreReadable(this) }

        val copiedRight = Mat(image, Rect(
                secondColumnStartX, secondColumnStartY,
                widthOfSecondColumn, heightOfSecondColumn
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