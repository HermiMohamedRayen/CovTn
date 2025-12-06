package com.iset.covtn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentHomeBinding
import com.iset.covtn.models.Ride
import com.iset.covtn.ui.viewModel.UserViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val userViewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var rides : MutableList<Ride> = mutableListOf()

        val adapter = RecycleViewAdapter(
            rides,
            getAdress = {
                ride -> getAdress(ride)
            },
            viewDetails = {
                ride -> viewDetails(ride)
            }
        )

        binding.homeList.layoutManager = LinearLayoutManager(requireContext())
        binding.homeList.adapter = adapter

        userViewModel.rides.observe(viewLifecycleOwner){
            value -> adapter.updateList(value)
        }
        userViewModel.getLatestRides()

        userViewModel.addresses.observe(viewLifecycleOwner){
            value -> adapter.updateAddresses(value)
        }



        return root
    }

    fun getAdress(ride: Ride){
        userViewModel.updateAdresses(ride)
    }

    fun viewDetails(ride: Ride){

        userViewModel.selectRide(ride)
        findNavController().navigate(R.id.rideDetails)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}