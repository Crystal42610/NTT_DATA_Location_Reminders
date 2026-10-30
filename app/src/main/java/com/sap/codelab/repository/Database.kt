package com.sap.codelab.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sap.codelab.model.Memo

/**
 * That database that is used to store information.
 */
@Database(entities = [Memo::class], version = 2, exportSchema = false)
internal abstract class Database : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao

    companion object {
        // Preserve existing notes while adding accurate coordinates and one-shot delivery state.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE memo ADD COLUMN locationLatitude REAL")
                db.execSQL("ALTER TABLE memo ADD COLUMN locationLongitude REAL")
                db.execSQL("ALTER TABLE memo ADD COLUMN notifiedAt INTEGER")
            }
        }
    }
}