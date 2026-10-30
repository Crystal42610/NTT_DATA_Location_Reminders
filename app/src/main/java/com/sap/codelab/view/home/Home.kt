package com.sap.codelab.view.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sap.codelab.utils.applySystemBarInsets
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.location.LocationPermissionHelper
import com.sap.codelab.location.ReminderMonitoring
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.view.create.CreateMemo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Explains and requests the permissions necessary for reminders after the app is closed. */
internal class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var model: HomeViewModel
    private lateinit var showAll: MenuItem
    private lateinit var showOpen: MenuItem

    private val finePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refreshPermissionBanner()
        if (LocationPermissionHelper.hasFineLocation(this)) requestNextPermission()
    }
    private val backgroundPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshPermissionBanner()
        if (LocationPermissionHelper.hasBackgroundLocation(this)) requestNextPermission()
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshPermissionBanner()
        syncReminders()
    }
    private val createMemo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) { model.refreshMemos(); syncReminders() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        binding.root.applySystemBarInsets()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[HomeViewModel::class.java]
        binding.contentHome.enablePermissions.setOnClickListener { requestNextPermission() }
        setupRecyclerView(initializeAdapter())
        binding.fab.setOnClickListener { createMemo.launch(Intent(this, CreateMemo::class.java)) }
        model.loadOpenMemos()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionBanner()
        syncReminders()
        if (::model.isInitialized) model.refreshMemos()
    }

    private fun refreshPermissionBanner() {
        val info = binding.contentHome
        info.permissionStatus.setText(when {
            !LocationPermissionHelper.hasFineLocation(this) -> R.string.permission_fine_needed
            !LocationPermissionHelper.hasBackgroundLocation(this) -> R.string.permission_background_needed
            !LocationPermissionHelper.notificationAllowed(this) -> R.string.permission_notifications_needed
            else -> R.string.permissions_ready
        })
        info.enablePermissions.isEnabled = !(LocationPermissionHelper.canMonitor(this) &&
            LocationPermissionHelper.notificationAllowed(this))
    }

    private fun requestNextPermission() {
        when {
            !LocationPermissionHelper.hasFineLocation(this) -> finePermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            !LocationPermissionHelper.hasBackgroundLocation(this) -> {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    backgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.background_permission_title)
                        .setMessage(R.string.background_permission_rationale)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.open_settings) { _, _ ->
                            LocationPermissionHelper.openAppSettings(this)
                        }.show()
                }
            }
            !LocationPermissionHelper.notificationAllowed(this) -> {
                if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Notifications were disabled in system settings rather than denied at runtime.
                    LocationPermissionHelper.openAppSettings(this)
                }
            }
            else -> syncReminders()
        }
    }

    private fun syncReminders() {
        if (!LocationPermissionHelper.canMonitor(this)) return
        lifecycleScope.launch {
            try { ReminderMonitoring(applicationContext).syncPending() }
            catch (e: Exception) {
                binding.contentHome.permissionStatus.setText(R.string.monitoring_error)
            }
        }
    }

    private fun initializeAdapter(): MemoAdapter {
        val adapter = MemoAdapter(mutableListOf(), { view ->
            showMemo((view.tag as Memo).id)
        }, { checkbox, checked ->
            val memo = checkbox.tag as Memo
            if (checked) lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    if (Repository.completeMemo(memo.id)) {
                        try { ReminderMonitoring(applicationContext).remove(memo.id) }
                        catch (_: Exception) { /* database state remains authoritative */ }
                    }
                }
                model.refreshMemos()
            }
        })
        lifecycleScope.launch { model.memos.collect { adapter.setItems(it) } }
        return adapter
    }

    private fun showMemo(id: Long) {
        startActivity(Intent(this, ViewMemo::class.java).putExtra(BUNDLE_MEMO_ID, id))
    }

    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home)
            this.adapter = adapter
            addItemDecoration(DividerItemDecoration(this@Home, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        showAll = menu.findItem(R.id.action_show_all)
        showOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_show_all -> {
            model.loadAllMemos(); showAll.isVisible = false; showOpen.isVisible = true; true
        }
        R.id.action_show_open -> {
            model.loadOpenMemos(); showOpen.isVisible = false; showAll.isVisible = true; true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
