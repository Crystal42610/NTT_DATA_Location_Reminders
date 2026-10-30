package com.sap.codelab.view.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.location.GeoRules
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Validates the complete memo before persisting it. No network work on the UI thread. */
internal class CreateMemoViewModel : ViewModel() {
    private var memo = Memo(0, "", "", 0, 0, 0, false)

    fun updateMemo(title: String, description: String, latitude: Double?, longitude: Double?) {
        memo = Memo(id = 0, title = title.trim(), description = description.trim(),
            reminderDate = 0, reminderLatitude = 0, reminderLongitude = 0,
            locationLatitude = latitude, locationLongitude = longitude)
    }

    fun isMemoValid() = !hasTitleError() && !hasTextError() && !hasLocationError()
    fun hasTitleError() = memo.title.isBlank()
    fun hasTextError() = memo.description.isBlank()
    fun hasLocationError() = !GeoRules.validPoint(memo.locationLatitude, memo.locationLongitude)

    suspend fun saveMemo(): Long = withContext(Dispatchers.IO) { Repository.saveMemo(memo) }
}
