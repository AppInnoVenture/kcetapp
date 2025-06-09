package com.kea.pyp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kea.pyp.databinding.FragmentPreviousYearPapersBinding

class UpdatesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPreviousYearPapersBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_previous_year_papers, container, false)
        
        val items = listOf(
            YearItem(getString(R.string.up_kcet), getString(R.string.up_kcet_pdf)),
            YearItem(getString(R.string.up_prac), getString(R.string.up_prac_pdf)),
            YearItem(getString(R.string.up_info), getString(R.string.up_info_pdf)),
            YearItem(getString(R.string.up_rank), getString(R.string.up_rank_pdf))
        )

        val itemAdapter = AdapterLan(items) { item ->
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra("desc", item.descLan)
            intent.putExtra("prefix", item.extra)
            intent.putExtra("diff", "up")
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = itemAdapter

        return binding.root
    }
}