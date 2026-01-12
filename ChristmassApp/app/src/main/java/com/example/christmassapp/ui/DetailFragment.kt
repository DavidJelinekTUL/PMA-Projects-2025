package com.example.christmassapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.christmassapp.R
import com.example.christmassapp.data.SoulRepository
import com.example.christmassapp.databinding.FragmentDetailBinding

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val args: DetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDetailBinding.bind(view)


        val soul = SoulRepository.getSoulById(args.soulId)

        soul?.let {
            binding.tvDetailName.text = it.name
            binding.tvDetailWishlist.text = "PŘÁNÍ: ${it.wishlist}"
            binding.tvDetailSins.text = "HŘÍCHY: ${it.sins}"


            binding.tvDetailVirtues.text = "DOBRA: ${it.virtues}"

            binding.tvDetailScore.text = "SKÓRE ZKAŽENOSTI: ${it.sinScore}/100"
        }


        binding.btnApprove.setOnClickListener {
            Toast.makeText(context, "Schváleno! Dárky letí.", Toast.LENGTH_SHORT).show()
        }

        binding.btnCoal.setOnClickListener {
            Toast.makeText(context, "Dostane uhlí! (A shnilé brambory)", Toast.LENGTH_SHORT).show()
        }
    }
}