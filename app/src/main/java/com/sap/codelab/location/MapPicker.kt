package com.sap.codelab.location

/** UI boundary: map library can be swapped without changing memo creation. */
internal interface MapPicker {
    fun setOnLocationSelected(listener: (latitude: Double, longitude: Double) -> Unit)
    fun showLocation(latitude: Double, longitude: Double)
    fun onResume()
    fun onPause()
    fun dispose()
}
