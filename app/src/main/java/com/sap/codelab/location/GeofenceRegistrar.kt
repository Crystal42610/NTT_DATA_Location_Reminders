package com.sap.codelab.location

import com.sap.codelab.model.Memo

/** Abstraction over the optional Play services provider. */
internal interface GeofenceRegistrar {
    suspend fun register(memos: List<Memo>)
    suspend fun unregister(memoId: Long)
}
