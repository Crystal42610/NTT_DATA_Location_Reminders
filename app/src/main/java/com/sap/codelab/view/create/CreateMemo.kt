package com.sap.codelab.view.create

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sap.codelab.utils.applySystemBarInsets
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.location.LocationPermissionHelper
import com.sap.codelab.location.MapPicker
import com.sap.codelab.location.OpenStreetMapPicker
import com.sap.codelab.location.ReminderMonitoring
import kotlinx.coroutines.launch
import java.util.Locale

/** Create a memo with a location explicitly selected by tapping the map. */
internal class CreateMemo : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private lateinit var map: MapPicker
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var saving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        binding.root.applySystemBarInsets()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]
        map = OpenStreetMapPicker(this, binding.contentCreateMemo.mapContainer)
        map.setOnLocationSelected { latitude, longitude ->
            selectedLatitude = latitude
            selectedLongitude = longitude
            binding.contentCreateMemo.selectedLocation.text = String.format(Locale.US,
                getString(R.string.location_selected), latitude, longitude)
        }
        val lat = savedInstanceState?.getDouble("latitude")
        val lon = savedInstanceState?.getDouble("longitude")
        if (lat != null && lon != null) map.showLocation(lat, lon)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectedLatitude?.let { outState.putDouble("latitude", it) }
        selectedLongitude?.let { outState.putDouble("longitude", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { map.onPause(); super.onPause() }
    override fun onDestroy() { map.dispose(); super.onDestroy() }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.action_save -> { saveMemo(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun saveMemo() {
        if (saving) return
        binding.contentCreateMemo.run {
            model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString(), selectedLatitude, selectedLongitude)
            memoTitleContainer.error = if (model.hasTitleError()) getString(R.string.memo_title_empty_error) else null
            memoDescriptionContainer.error = if (model.hasTextError()) getString(R.string.memo_text_empty_error) else null
            if (model.hasLocationError()) selectedLocation.setText(R.string.location_required)
        }
        if (!model.isMemoValid()) return
        saving = true
        lifecycleScope.launch {
            try {
                model.saveMemo() // Wait for persistence before returning success.
                if (LocationPermissionHelper.canMonitor(this@CreateMemo) && LocationPermissionHelper.notificationAllowed(this@CreateMemo)) {
                    try { ReminderMonitoring(applicationContext).syncPending() }
                    catch (e: Exception) { Toast.makeText(this@CreateMemo,
                        getString(R.string.monitoring_error), Toast.LENGTH_LONG).show() }
                } else {
                    Toast.makeText(this@CreateMemo, R.string.enable_background_reminders, Toast.LENGTH_LONG).show()
                }
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: Exception) {
                saving = false
                Toast.makeText(this@CreateMemo, getString(R.string.save_failed), Toast.LENGTH_LONG).show()
            }
        }
    }
}
