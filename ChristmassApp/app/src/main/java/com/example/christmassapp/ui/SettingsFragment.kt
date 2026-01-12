package com.example.christmassapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.christmassapp.R
import com.example.christmassapp.data.GodModeManager
import com.example.christmassapp.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSettingsBinding.bind(view)

        val godManager = GodModeManager(requireContext())

        lifecycleScope.launch {
            godManager.apocalypseFlow.collect { isBroke ->
                binding.switchApocalypse.isChecked = isBroke

                if (isBroke) {
                    // Stav: Došly prachy
                    binding.tvStatus.text = "STAV ÚČTU: BANKROT (Ježíšek je na pracáku)"
                    binding.root.setBackgroundColor(0xFFFFCCCC.toInt()) // Světle červená
                } else {
                    // Stav: Vše OK
                    binding.tvStatus.text = "STAV ÚČTU: SOLVENTNÍ (Dárky budou!)"
                    binding.root.setBackgroundColor(0xFFFFFFFF.toInt()) // Bílá
                }
            }
        }

        binding.switchApocalypse.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                godManager.setApocalypseMode(isChecked)
            }
        }
    }
}