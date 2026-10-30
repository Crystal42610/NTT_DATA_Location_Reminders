package com.sap.codelab.view.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sap.codelab.utils.applySystemBarInsets
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.model.Memo
import kotlinx.coroutines.launch
import java.util.Locale

internal const val BUNDLE_MEMO_ID = "memoId"

internal class ViewMemo : AppCompatActivity() {
    private lateinit var binding: ActivityViewMemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        binding.root.applySystemBarInsets()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.contentCreateMemo.mapContainer.visibility = View.GONE
        binding.contentCreateMemo.mapPrompt.visibility = View.GONE
        val model = ViewModelProvider(this)[ViewMemoViewModel::class.java]
        lifecycleScope.launch { model.memo.collect { it?.let(::updateUI) } }
        model.loadMemo(intent.getLongExtra(BUNDLE_MEMO_ID, -1))
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun updateUI(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
            selectedLocation.text = if (memo.locationLatitude != null && memo.locationLongitude != null)
                String.format(Locale.US, getString(R.string.location_selected), memo.locationLatitude, memo.locationLongitude)
                else getString(R.string.location_unavailable)
        }
    }
}
