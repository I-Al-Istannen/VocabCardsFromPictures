package me.ialistannen.vocabcardfrompicture.ocr

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

fun Mat.toBufferedImage(): BufferedImage {
    val matOfBye = MatOfByte()
    Imgcodecs.imencode(".png", this, matOfBye)

    return ImageIO.read(ByteArrayInputStream(matOfBye.toArray()))
}