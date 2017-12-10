package me.ialistannen.vocabcardfrompicture.util

fun divideInRanges(numbers: List<Double>, rangeSize: Double): List<Bucket> {
    if (numbers.isEmpty()) {
        return emptyList()
    }
    val buckets = mutableListOf<Bucket>()

    for (number in numbers) {
        val bucketIndex = Math.floor(number / rangeSize).toInt()
        addToBucketOrCreate(buckets, number, bucketIndex)
    }

    return buckets.toList()
}

private fun addToBucketOrCreate(buckets: MutableList<Bucket>, number: Double, index: Int) {
    val bucket = buckets.find { it.index == index }

    if (bucket != null) {
        bucket.contents.add(number)
        return
    }

    buckets.add(
            Bucket(index, mutableListOf(number))
    )
}

data class Bucket(val index: Int, val contents: MutableList<Double>) {

    fun min() = contents.min()

    fun max() = contents.max()
}