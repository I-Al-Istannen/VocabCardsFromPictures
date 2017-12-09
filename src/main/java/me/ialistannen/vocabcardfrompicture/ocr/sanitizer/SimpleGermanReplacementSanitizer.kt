package me.ialistannen.vocabcardfrompicture.ocr.sanitizer

/**
 * Replaces common errors with their (hopefully) right char.
 */
class SimpleGermanReplacementSanitizer : BasicOcrResultSanitizer() {

    init {
        replacements.putAll(mapOf(
        ))
    }
}