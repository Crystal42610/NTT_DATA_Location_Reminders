package com.sap.codelab.notifications

/** Unicode-safe first 140 characters, without splitting surrogate pairs. */
internal object NotePreview {
    const val MAX_CHARACTERS = 140
    fun firstCharacters(text: String, limit: Int = MAX_CHARACTERS): String {
        require(limit >= 0)
        val count = text.codePointCount(0, text.length)
        return text.substring(0, text.offsetByCodePoints(0, minOf(count, limit)))
    }
}
