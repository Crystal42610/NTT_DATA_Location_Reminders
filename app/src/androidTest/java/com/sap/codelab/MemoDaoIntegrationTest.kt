package com.sap.codelab

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Database
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Confirms the database's single-delivery guard with real SQLite on an Android device. */
@RunWith(AndroidJUnit4::class)
class MemoDaoIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = Room.inMemoryDatabaseBuilder(context, Database::class.java)
        .allowMainThreadQueries().build()

    @After fun tearDown() = db.close()

    @Test fun claimNotificationOnlyOnceAndKeepCoordinates() {
        val dao = db.getMemoDao()
        val memoId = dao.insert(Memo(0, "Arrive at office", "Remember the documents", 0, 0, 0,
            locationLatitude = 47.1625, locationLongitude = 19.5033))
        assertEquals(1, dao.claimNotification(memoId, 1000L))
        assertEquals(0, dao.claimNotification(memoId, 2000L))
        val saved = dao.getMemoById(memoId)
        assertNotNull(saved)
        assertEquals(1000L, saved!!.notifiedAt)
        assertEquals(47.1625, saved.locationLatitude!!, 0.000000001)
        assertEquals(19.5033, saved.locationLongitude!!, 0.000000001)
    }

    @Test fun completingMemoNeverClearsAClaimedNotification() {
        val dao = db.getMemoDao()
        val memoId = dao.insert(Memo(0, "Bank", "Pick up documents", 0, 0, 0,
            locationLatitude = 42.6977, locationLongitude = 23.3219))
        assertEquals(1, dao.claimNotification(memoId, 1000L))
        assertEquals(1, dao.completeMemo(memoId))
        assertEquals(true, dao.getMemoById(memoId)?.isDone)
        assertEquals(1000L, dao.getMemoById(memoId)?.notifiedAt)
        assertEquals(0, dao.completeMemo(memoId))
        assertEquals(0, dao.claimNotification(memoId, 2000L))
    }

    @Test fun completionPreventsNotificationEvenBeforeDelivery() {
        val dao = db.getMemoDao()
        val memoId = dao.insert(Memo(0, "Meeting", "Already done", 0, 0, 0,
            locationLatitude = 47.1625, locationLongitude = 19.5033))
        assertEquals(1, dao.completeMemo(memoId))
        assertEquals(0, dao.claimNotification(memoId, 1000L))
    }

    @Test fun completedMemoCannotBeNotified() {
        val dao = db.getMemoDao()
        val memoId = dao.insert(Memo(0, "Completed", "Already done", 0, 0, 0,
            isDone = true, locationLatitude = 0.0, locationLongitude = 0.0))
        assertEquals(0, dao.claimNotification(memoId, 1000L))
        assertNull(dao.getMemoById(memoId)?.notifiedAt)
    }
}
