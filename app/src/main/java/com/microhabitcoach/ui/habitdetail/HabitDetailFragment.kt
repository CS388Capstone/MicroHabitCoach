package com.microhabitcoach.ui.habitdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.microhabitcoach.R
import com.microhabitcoach.databinding.FragmentHabitDetailBinding

class HabitDetailFragment : Fragment() {

    private var _binding: FragmentHabitDetailBinding? = null
    private val binding get() = _binding!!

    private val args: HabitDetailFragmentArgs by navArgs()

    private val viewModel: HabitDetailViewModel by viewModels {
        HabitDetailViewModel.Factory(requireActivity().application, args.habitId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupEditButton()
        observeViewModel()
        viewModel.loadHabit()
    }

    private fun setupEditButton() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                HabitDetailFragmentDirections.actionHabitDetailFragmentToAddEditHabitFragment(
                    habitId = args.habitId
                )
            )
        }
    }

    private fun observeViewModel() {
        // TODO: Observe habit detail data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

