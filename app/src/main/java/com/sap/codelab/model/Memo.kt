package com.sap.codelab.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a memo.
 */
@Entity(tableName = "memo")
internal data class Memo(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "description")
        var description: String,
        @ColumnInfo(name = "reminderDate")
        var reminderDate: Long,
        @ColumnInfo(name = "reminderLatitude")
        var reminderLatitude: Long,
        @ColumnInfo(name = "reminderLongitude")
        var reminderLongitude: Long,
        @ColumnInfo(name = "isDone")
        var isDone: Boolean = false,
        // Nullable location is deliberate: earlier memos had no precise selected point.
        @ColumnInfo(name = "locationLatitude")
        var locationLatitude: Double? = null,
        @ColumnInfo(name = "locationLongitude")
        var locationLongitude: Double? = null,
        @ColumnInfo(name = "notifiedAt")
        var notifiedAt: Long? = null
)
