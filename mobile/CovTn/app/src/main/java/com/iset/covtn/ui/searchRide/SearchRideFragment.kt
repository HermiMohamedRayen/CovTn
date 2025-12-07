package com.iset.covtn.ui.searchRide

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import java.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentSearchRideBinding
import com.iset.covtn.ui.viewModel.UserViewModel

class SearchRideFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentSearchRideBinding? = null

    private val userViewModel : UserViewModel by activityViewModels()
    private val binding get() = _binding!!

    private var dep: Marker? = null
    private var dest: Marker? = null

    private val depTime = Calendar.getInstance()

    private val destTime = depTime.clone() as Calendar

    private val origin = destTime.clone() as Calendar

    private lateinit var btn: ExtendedFloatingActionButton;

    private var select: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchRideBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapfrag = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapfrag.getMapAsync(this)

        btn = activity?.findViewById(R.id.fab)!!



        btn.setOnClickListener {
            startevent()
        }

        binding.setdeptime.setOnClickListener { showTimeDialog(it as TextView,origin,depTime) }

        binding.setdesttime.setOnClickListener { showTimeDialog(it as TextView,depTime,destTime) }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.7065, 9.5815), 7f))
        googleMap.setOnMapClickListener { latLng ->
            when (select) {
                0 -> {
                    if (dep == null) {
                        dep = googleMap.addMarker(MarkerOptions().position(latLng).title("departure"))
                    } else {
                        dep?.position = latLng

                    }
                }

                1 -> {
                    if (dest == null) {
                        dest = googleMap.addMarker(MarkerOptions().position(latLng).title("destination"))
                    } else {
                        dest?.position = latLng
                    }
                }

            }
        }
    }

    fun startevent(){
        if (dep == null) {
            Snackbar.make(requireView(), "please set ride departure", Snackbar.LENGTH_LONG)
                .show()
        } else {
            select = 1
            btn.text = "set destination"

            btn.setOnClickListener {
                if (dest == null) {
                    Snackbar.make(
                        requireView(),
                        "please set ride destination",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    select = 2
                    btn.text = "Go"
                    btn.icon = AppCompatResources.getDrawable(requireContext(),R.drawable.search)
                    btn.setOnClickListener { performSearch() }
                }
            }
        }
    }


    fun performSearch() {
        if(depTime.equals(origin) || destTime.equals(origin)){
            Toast.makeText(requireContext(),"please provide a departure time and an arrival time",
                Toast.LENGTH_LONG).show()
            return
        }
        if(destTime.before(depTime)){
            Toast.makeText(requireContext(),"arrival time must be after departure time",
                Toast.LENGTH_LONG).show()
            return
        }
        userViewModel.search(dep?.position!!,dest?.position!!,depTime,destTime)
        findNavController().navigate(R.id.nav_search_result)
        select = 0

    }

    fun showTimeDialog(view : TextView, prevCalendar: Calendar ,calendar : Calendar){
        val date = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                TimePickerDialog(
                    requireContext(),
                    { _, h, min ->
                        if(Calendar.getInstance().before(
                                Calendar.getInstance().apply {
                                    set(y, m, d, h, min,0)
                                }
                            )){

                            val final = "%02d/%02d/%04d %02d:%02d".format(d, m + 1, y, h, min)
                            view.text = final
                            calendar.set(y, m, d, h, m,0)
                        }else{
                            Toast.makeText(requireContext(),"please select a date in the future",
                                Toast.LENGTH_LONG).show()
                        }
                    },
                    prevCalendar.get(Calendar.HOUR_OF_DAY),
                    prevCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            prevCalendar.get(Calendar.YEAR),
            prevCalendar.get(Calendar.MONTH),
            prevCalendar.get(Calendar.DAY_OF_MONTH)
        )
        date.datePicker.minDate = prevCalendar.time.time
        date.show()


    }

}