package me.ialistannen.vocabcardfrompicture.ocr.sanitizer

class SimpleLatinReplacementSanitizer : BasicOcrResultSanitizer() {

    init {
        replacements.putAll(mapOf(
                Regex("(?<=us,a,).m") to "um",
                Regex("€") to "e",
                Regex("[1-9]") to ""
        ))
    }
}