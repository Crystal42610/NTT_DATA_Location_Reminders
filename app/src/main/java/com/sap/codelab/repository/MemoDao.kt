package com.sap.codelab.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sap.codelab.model.Memo

/**
 * The Dao representation of a Memo.
 */
@Dao
internal interface MemoDao {

    /**
     * @return all memos that are currently in the database.
     */
    @Query("SELECT * FROM memo")
    fun getAll(): List<Memo>

    /**
     * @return all memos that are currently in the database and have not yet been marked as "done".
     */
    @Query("SELECT * FROM memo WHERE isDone = 0")
    fun getOpen(): List<Memo>

    /**
     * Inserts the given Memo into the database. We currently do not support updating of memos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: Memo): Long

    /**
     * @return the memo whose id matches the given id.
     */
    @Query("SELECT * FROM memo WHERE id = :memoId")
    fun getMemoById(memoId: Long): Memo?

    @Query("SELECT * FROM memo WHERE isDone = 0 AND notifiedAt IS NULL AND locationLatitude IS NOT NULL AND locationLongitude IS NOT NULL")
    fun getPendingLocationMemos(): List<Memo>

    // Update only completion state. Replacing the entire row could clear notifiedAt
    // when a geofence callback and user completion happen at the same time.
    @Query("UPDATE memo SET isDone = 1 WHERE id = :memoId AND isDone = 0")
    fun completeMemo(memoId: Long): Int

    // Atomic claim: multiple ENTER broadcasts can only notify once.
    @Query("UPDATE memo SET notifiedAt = :time WHERE id = :memoId AND isDone = 0 AND notifiedAt IS NULL")
    fun claimNotification(memoId: Long, time: Long): Int
}