package me.ialistannen.vocabcardfrompicture.ocr.sanitizer

/**
 * Sanitizes the result of the character recognition.
 */
interface OcrResultSanitizer {

    fun sanitize(input: String): String
}

abstract class BasicOcrResultSanitizer : OcrResultSanitizer {
    protected val replacements: MutableMap<Regex, String> = LinkedHashMap()

    init {
        replacements.putAll(mapOf(
                Regex("\u201A") to ",",
                Regex(" ' ") to ""
        ))
    }

    override fun sanitize(input: String): String {
        var result = input
        for ((pattern, replacement) in replacements) {
            result = pattern.replace(result, replacement)
        }

        return result
    }
}