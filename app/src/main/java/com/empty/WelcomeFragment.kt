package com.empty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.empty.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        binding.goToMapsButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_mapsFragment)
        }

        binding.goToCameraButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_cameraActivity)
        }

        return binding.root
    }
}