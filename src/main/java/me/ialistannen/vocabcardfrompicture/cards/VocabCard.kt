package me.ialistannen.vocabcardfrompicture.cards

/**
 * A card with a meaning and some forms
 */
data class VocabCard(val shortTeaser: String, val withForms: String, val meaning: String) {

    override fun toString(): String {
        return "Vocab['$withForms' ($shortTeaser) => '$meaning']"
    }
}