package me.ialistannen.vocabcardfrompicture

import java.io.File

fun main(args: Array<String>) {
    System.load("/usr/share/opencv/java/libopencv_java331.so")
    val latinImage = File("/tmp/lat/Unmodified_image.jpg")
    val germanImageFile = File("/tmp/lat/Unmodified_image_german.jpg")

    CardReader().readCards(germanImageFile, latinImage).subscribe(
            {
                for (card in it) {
                    println(card)
                }
            },
            { it.printStackTrace() }
    )
}