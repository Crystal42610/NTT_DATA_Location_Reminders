package com.sap.codelab.location

import android.content.Context
import android.widget.FrameLayout
import android.view.MotionEvent
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** OpenStreetMap adapter. The rest of the app depends only on [MapPicker]. */
internal class OpenStreetMapPicker(context: Context, container: FrameLayout) : MapPicker {
    private val map: MapView
    private var marker: Marker? = null
    private var onSelected: (Double, Double) -> Unit = { _, _ -> }

    init {
        Configuration.getInstance().userAgentValue = context.packageName
        map = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Let users drag the map without the surrounding form ScrollView intercepting.
            setOnTouchListener { view, event ->
                view.parent?.requestDisallowInterceptTouchEvent(
                    event.actionMasked != MotionEvent.ACTION_UP && event.actionMasked != MotionEvent.ACTION_CANCEL
                )
                false
            }
            controller.setZoom(5.5)
            controller.setCenter(GeoPoint(47.1625, 19.5033)) // Central Europe; not a chosen location.
        }
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                pick(point)
                return true
            }
            override fun longPressHelper(point: GeoPoint): Boolean = false
        }))
        container.addView(map, FrameLayout.LayoutParams(-1, -1))
    }

    private fun pick(point: GeoPoint) {
        marker?.let { map.overlays.remove(it) }
        marker = Marker(map).apply {
            position = point
            title = "Reminder location"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)
        map.invalidate()
        onSelected(point.latitude, point.longitude)
    }

    override fun showLocation(latitude: Double, longitude: Double) {
        val point = GeoPoint(latitude, longitude)
        pick(point)
        map.controller.setCenter(point)
        map.controller.setZoom(15.0)
    }

    override fun setOnLocationSelected(listener: (Double, Double) -> Unit) { onSelected = listener }
    override fun onResume() = map.onResume()
    override fun onPause() = map.onPause()
    override fun dispose() = map.onDetach()
}
