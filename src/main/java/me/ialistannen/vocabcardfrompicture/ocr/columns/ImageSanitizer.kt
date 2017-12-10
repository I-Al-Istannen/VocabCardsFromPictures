package me.ialistannen.vocabcardfrompicture.ocr.columns

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Helps with sanitizing images,
 */
internal class ImageSanitizer {

    /**
     * Sanitizes the image so contour detection is easier.
     *
     * @return A modified clone of the input image
     */
    fun sanitizeForContourDetection(sourceImage: Mat): Mat {
        val image = sourceImage.clone()

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

    /**
     * Enhances the image. This makes text more readable, removes imperfections but tries to not
     * alter the information in the image.
     *
     * @return A modified clone of the input image
     */
    fun enhance(sourceImage: Mat): Mat {
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
}