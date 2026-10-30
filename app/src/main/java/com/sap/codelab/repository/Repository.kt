package com.sap.codelab.repository

import androidx.room.Room
import android.content.Context
import androidx.annotation.WorkerThread
import com.sap.codelab.model.Memo

private const val DATABASE_NAME: String = "codelab"

/**
 * The repository is used to retrieve data from a data source.
 */
internal object Repository : IMemoRepository {

    private lateinit var database: Database

    fun initialize(applicationContext: Context) {
        database = Room.databaseBuilder(applicationContext, Database::class.java, DATABASE_NAME)
            .addMigrations(Database.MIGRATION_1_2).build()
    }

    @WorkerThread
    override fun saveMemo(memo: Memo): Long = database.getMemoDao().insert(memo)

    @WorkerThread
    override fun getOpen(): List<Memo> = database.getMemoDao().getOpen()

    @WorkerThread
    override fun getAll(): List<Memo> = database.getMemoDao().getAll()

    @WorkerThread
    override fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id)
        ?: throw NoSuchElementException("Memo $id not found")

    @WorkerThread
    fun pendingLocationMemos(): List<Memo> = database.getMemoDao().getPendingLocationMemos()

    @WorkerThread
    fun completeMemo(memoId: Long): Boolean = database.getMemoDao().completeMemo(memoId) == 1

    @WorkerThread
    fun claimNotification(memoId: Long): Memo? {
        val dao = database.getMemoDao()
        if (dao.claimNotification(memoId, System.currentTimeMillis()) != 1) return null
        return dao.getMemoById(memoId)
    }
}
