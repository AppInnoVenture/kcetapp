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

class CutoffFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPreviousYearPapersBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_previous_year_papers, container, false)

        val items = listOf(
            Item(R.drawable.mbbs, getString(R.string.mbbs),"medi_"),
            Item(R.drawable.engg, getString(R.string.engg),"engg_"),
            Item(R.drawable.vet, getString(R.string.vet),"vet_"),
            Item(R.drawable.agri, getString(R.string.agri),"agri_"),
            Item(R.drawable.dental, getString(R.string.bds),"dental_"),
            Item(R.drawable.ayush, getString(R.string.ayu),"ismh_"),
            Item(R.drawable.pharma, getString(R.string.phar), "pharma_"),
            Item(R.drawable.arch, getString(R.string.arch),"arch_"),
            Item(R.drawable.bnys, getString(R.string.bnys),"bnys_"),
            Item(R.drawable.nurs, getString(R.string.nurs),"nurs_"),
            Item(R.drawable.ahs, getString(R.string.ahs),"ahs_")
        )        

        val itemAdapter = ItemAdapter(items){
                item ->
            val intent = Intent(context, CutoffActivity::class.java)
            intent.putExtra("extraTag",item.extra)
            intent.putExtra("desc", item.description)
            intent.putExtra("img",item.img)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = itemAdapter

        return binding.root
    }
}