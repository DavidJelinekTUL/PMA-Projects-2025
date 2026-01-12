package com.example.christmassapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.christmassapp.R
import com.example.christmassapp.data.SoulRepository
import com.example.christmassapp.databinding.FragmentListBinding

class ListFragment : Fragment(R.layout.fragment_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentListBinding.bind(view)

        val adapter = MortalAdapter(SoulRepository.souls) { soulId ->
            val action = ListFragmentDirections.actionListFragmentToDetailFragment(soulId)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }
}