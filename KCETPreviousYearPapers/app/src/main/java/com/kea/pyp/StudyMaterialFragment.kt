package com.kea.pyp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.kea.pyp.databinding.FragmentPreviousYearPapersBinding

class StudyMaterialFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPreviousYearPapersBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_previous_year_papers, container, false)
            val items = listOf(
            Item(R.drawable.phy, getString(R.string.phy), "phyno"),
            Item(R.drawable.che, getString(R.string.che), "cheno"),
            Item(R.drawable.mat, getString(R.string.mat), "matno"),
            Item(R.drawable.bio, getString(R.string.bio), "biono"),
            Item(R.drawable.agri, getString(R.string.vetno),"agri")
            )

        val itemAdapter = ItemAdapter(items) { item ->
            val intent = Intent(context, NotesActivity::class.java)
            intent.putExtra("extraTag", item.extra)
            intent.putExtra("desc", item.description)
            intent.putExtra("img",item.img)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = itemAdapter

        return binding.root
    }
}