package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ThreadLocalRandom


/**
 *
 */
class ColumnFinder

fun main(args: Array<String>) {
    println("Test")

    System.load("/usr/share/opencv/java/libopencv_java331.so")

    val readImage = Imgcodecs.imread(
//            "/tmp/lat/DOC-20171206-194617.jpg", Imgcodecs.IMREAD_GRAYSCALE
            "/tmp/lat/Unmodified_image.jpg", Imgcodecs.IMREAD_GRAYSCALE
    )

    val enhancedImage = enhanceImage(readImage)
    var targetMat = sanitizeImage(enhancedImage)

    val contours = findContoursAboveWidth(targetMat, 200)

    val hm = Mat(targetMat.size(), Imgproc.COLOR_BGR2RGB)
    targetMat.copyTo(hm)
    targetMat = hm

    for ((index, _) in contours.withIndex()) {
        val rng = ThreadLocalRandom.current()
        val color = Scalar(
                rng.nextInt(0, 255).toDouble(),
                rng.nextInt(0, 255).toDouble(),
                rng.nextInt(0, 255).toDouble()
        )
        Imgproc.drawContours(targetMat, contours, index, color)
    }

    val columnStarts = findColumnStarts(contours)

    println(columnStarts)

    for (min in columnStarts) {
        Imgproc.line(
                targetMat,
                Point(min - 5.0, 0.0), Point(min - 5.0, targetMat.height().toDouble()),
                Scalar(255.0, 255.0, 255.0),
                4
        )
    }


    run {
        getColumnImages(columnStarts, enhancedImage)
    }

    Imgcodecs.imwrite("/tmp/test.png", targetMat)
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

private fun findContoursAboveWidth(targetMat: Mat, minWidth: Int): List<MatOfPoint> {
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
            println("Added in if: $lastMinX")
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
    Imgcodecs.imwrite("/tmp/testleft.png", copiedLeft)

    val copiedRight = Mat(image, Rect(
            secondColumnStart, 0,
            widthOfSecondColumn, image.height()
    ))
    Imgcodecs.imwrite("/tmp/testright.png", copiedRight)

    return listOf(copiedLeft, copiedRight)
}
