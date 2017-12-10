package me.ialistannen.vocabcardfrompicture.parsing

import me.ialistannen.vocabcardfrompicture.cards.VocabCard

/**
 * Parses two Lists of Strings to vocab cards.
 */
class StringToCardParser {

    companion object {
        private val BASE_FROM_EXTRACTOR: Regex = Regex("(.+?)\\W.+")
        private val AMOUNT_OF_MEANINGS_REMOVER: Regex = Regex("(.+)(\\s*\\d)\$")
    }

    /**
     * Combines a list of matching vocabs from the latin and german list to [VocabCard]s.
     *
     * @param latin The words in latin
     * @param german The words in german. Same order as in latin!
     *
     * @throws IllegalArgumentException if the two lists are not of the same length
     * @return a list with the generated [VocabCard]s
     */
    fun parse(latin: List<String>, german: List<String>): List<VocabCard> {
        if (latin.size != german.size) {
            throw IllegalArgumentException("The two lists are not of the same length")
        }
        val result = mutableListOf<VocabCard>()

        for ((forms, meaning) in latin.zip(german)) {
            result.add(
                    VocabCard(
                            stripAdditionalForms(forms),
                            clearAmountOfMeaningsFromForm(forms),
                            meaning
                    )
            )
        }

        return result
    }

    private fun stripAdditionalForms(form: String): String {
        return BASE_FROM_EXTRACTOR.find(form)?.groupValues?.get(1) ?: form
    }

    private fun clearAmountOfMeaningsFromForm(form: String): String {
        return AMOUNT_OF_MEANINGS_REMOVER.replace(form, "$1").trim()
    }
}