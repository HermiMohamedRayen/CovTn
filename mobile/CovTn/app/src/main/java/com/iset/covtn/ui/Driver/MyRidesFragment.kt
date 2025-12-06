package com.iset.covtn.ui.Driver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentMyRidesBinding
import com.iset.covtn.models.Ride
import com.iset.covtn.ui.home.MyParticipationsAdapter
import com.iset.covtn.ui.home.RecycleViewAdapter
import com.iset.covtn.ui.viewModel.UserViewModel

class MyRidesFragment : Fragment() {

    private lateinit var binding: FragmentMyRidesBinding

    private val userViewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyRidesBinding.inflate(inflater,container,false)



        var rides : MutableList<Ride> = mutableListOf()

        val adapter = MyParticipationsAdapter(
            rides,
            getAdress = { ride ->
                getAdress(ride)
            },
            viewDetails = { ride ->
                viewDetails(ride)
            },
            unParticipate = {
                    ride, i -> remove(ride)
            }
        )

        binding.homeList.layoutManager = LinearLayoutManager(requireContext())
        binding.homeList.adapter = adapter

        userViewModel.rides.observe(viewLifecycleOwner){
                value -> adapter.updateList(value)
        }
        userViewModel.getMyRides()

        userViewModel.addresses.observe(viewLifecycleOwner){
                value -> adapter.updateAddresses(value)
        }




        return binding.root
    }


    fun getAdress(ride: Ride){
        userViewModel.updateAdresses(ride)
    }

    fun viewDetails(ride: Ride){

        userViewModel.selectRide(ride)
        findNavController().navigate(R.id.rideDetails)
    }

    fun remove(ride: Ride){
        val d = AlertDialog.Builder(requireContext())
        d.setTitle("Remove Ride")
        d.setMessage("do you want to remove this ride ?")
        d.setPositiveButton("yes"){
                dialog, which -> userViewModel.removeMyRide(ride)
        }
        d.setNegativeButton("no",null)
        d.create().show()

    }



}