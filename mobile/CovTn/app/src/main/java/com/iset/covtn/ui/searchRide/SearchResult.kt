package com.iset.covtn.ui.searchRide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentSearchResultBinding
import com.iset.covtn.models.Ride
import com.iset.covtn.ui.home.RecycleViewAdapter
import com.iset.covtn.ui.viewModel.UserViewModel

class SearchResult : Fragment() {

    private lateinit var binding: FragmentSearchResultBinding

    private val userViewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchResultBinding.inflate(inflater,container,false)

        val adapter = RecycleViewAdapter(
            mutableListOf(),
            getAdress = {
                ride -> userViewModel.updateAdresses(ride)
            },
            viewDetails = {ride -> viewDetail(ride)}
        )

        userViewModel.rides.observe(viewLifecycleOwner){
            value -> adapter.updateList(value)
        }

        userViewModel.addresses.observe(viewLifecycleOwner){
                value -> adapter.updateAddresses(value)
        }

        binding.searchResult.adapter = adapter
        binding.searchResult.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    fun viewDetail(ride: Ride){
        userViewModel.selectRide(ride)
        findNavController().navigate(R.id.rideDetails)
    }


}