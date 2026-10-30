package com.sap.codelab

import com.sap.codelab.notifications.NotePreview
import org.junit.Assert.*
import org.junit.Test

class NotePreviewTest {
    @Test fun shortTextIsUnchanged() {
        assertEquals("Buy groceries", NotePreview.firstCharacters("Buy groceries"))
    }

    @Test fun exactly140CharactersAreReturned() {
        val text = "x".repeat(140) + "more"
        assertEquals("x".repeat(140), NotePreview.firstCharacters(text))
    }

    @Test fun surrogatePairsAreNotSplit() {
        val text = "😃".repeat(145)
        assertEquals("😃".repeat(140), NotePreview.firstCharacters(text))
        assertEquals(140, NotePreview.firstCharacters(text).codePointCount(0,
            NotePreview.firstCharacters(text).length))
    }

    @Test fun emptyDescriptionReturnsEmptyPreview() {
        assertEquals("", NotePreview.firstCharacters(""))
    }
}
