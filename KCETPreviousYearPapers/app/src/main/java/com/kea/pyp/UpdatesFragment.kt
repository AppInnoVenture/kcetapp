package com.kea.pyp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kea.pyp.databinding.FragmentUpdatesBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpdatesFragment : Fragment() {

    private lateinit var binding: FragmentUpdatesBinding
    private val viewModel: UpdatesViewModel by viewModels { UpdatesViewModelFactory(requireActivity().application) }
    private lateinit var adapter: UpdatesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdatesBinding.inflate(inflater, container, false)

        setupAdapter()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        if (savedInstanceState == null) {
            viewModel.loadData(isSwipeRefresh = false)
        } 
        return binding.root
    }

    private fun setupAdapter() {
        adapter = UpdatesAdapter { item ->
            if (!isAdded || context == null) return@UpdatesAdapter
            when (item.type) {
                "url" -> {
                    if (!viewModel.checkInternetAvailability()) {
                        showRecursiveSnackbarForItem(item)
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, item.content.toUri())
                            startActivity(Intent.createChooser(intent, "Open with..."))
                            Toast.makeText(context, R.string.opening_browser_chooser, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error opening URL", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                "pdf" -> {
                    if (!item.offlineView && !File(requireContext().filesDir, item.pdfFilename).exists() && !viewModel.checkInternetAvailability()) {
                        showRecursiveSnackbarForItem(item)
                    } else {
                        val intent = Intent(context, PdfViewerActivity::class.java)
                        intent.putExtra("desc", item.description)
                        intent.putExtra("prefix", item.pdfFilename)
                        intent.putExtra("diff", "up")
                        intent.putExtra("name", item.content)
                        intent.putExtra("offL", item.offlineView)
                        startActivity(intent)
                    }
                }
                "toast" -> Toast.makeText(context, item.content, Toast.LENGTH_SHORT).show()
                "snack" -> {
                    if (binding.root.isAttachedToWindow) {
                        Snackbar.make(binding.recyclerView, item.content, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.loadData(isSwipeRefresh = true)
        }
    }

    private fun observeViewModel() {
    try {
        val offlineItems = listOf(
            InfoItem(
                id = -1,
                description = getString(R.string.up_kcet),
                type = "pdf",
                content = "",
                pdfFilename = getString(R.string.up_kcet_pdf),
                offlineView = true
            ),
            InfoItem(
                id = -2,
                description = getString(R.string.up_prac),
                type = "pdf",
                content = "",
                pdfFilename = getString(R.string.up_prac_pdf),
                offlineView = true
            ),
            InfoItem(
                id = -3,
                description = getString(R.string.up_info),
                type = "pdf",
                content = "",
                pdfFilename = getString(R.string.up_info_pdf),
                offlineView = true
            ),
            InfoItem(
                id = -4,
                description = getString(R.string.up_rank),
                type = "pdf",
                content = "",
                pdfFilename = getString(R.string.up_rank_pdf),
                offlineView = true
            )
        )

        viewModel.infoItemsLiveData.observe(viewLifecycleOwner) { items ->
            if (!isAdded || view == null) return@observe
            adapter.submitList(items + offlineItems)
        }

          // some private codes are removed, currently it shows only simplified version 
    
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isAdded || view == null) return@observe
            binding.swipeRefreshLayout.isRefreshing = isLoading == true
        }
    } catch (e: Exception) {
        viewModel.loadData(isSwipeRefresh = false)
    }
}

}