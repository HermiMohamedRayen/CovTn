package com.iset.covtn.ui.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentRideDetailBinding
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.GeoLocation
import com.iset.covtn.models.Ride
import com.iset.covtn.models.User
import com.iset.covtn.services.MapServices
import com.iset.covtn.services.UserService
import com.iset.covtn.ui.viewModel.UserViewModel
import java.io.File
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.iset.covtn.models.Rating

class ride_detail : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentRideDetailBinding

    private val userViewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRideDetailBinding.inflate(inflater,container,false)
        val ride = userViewModel.selectedRide.value!!

        userViewModel.fetchGeoCode(ride.departure).observe(viewLifecycleOwner){
            value -> binding.valueDeparture.text = value.display_name
        }
        userViewModel.fetchGeoCode(ride.destination).observe(viewLifecycleOwner){
                value -> binding.valueDestination.text = value.display_name
        }

        val image = MutableLiveData<File>()

        image.observe(viewLifecycleOwner){
                value -> if(value != null){
                    binding.driverImage.setImageBitmap(BitmapFactory.decodeFile(value.absolutePath))
                    binding.driverImage.setBackgroundColor(Color.WHITE)
                }
        }

        val driver = ride.driver
        binding.driverFullName.text = "${driver.firstName} ${driver.lastName}"

        userViewModel.getUserImage(requireContext(),driver.profilePicture,driver.email,image)

        val mapfrag = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapfrag.getMapAsync(this)

        binding.driverInfo.setOnClickListener {
            findNavController().navigate(R.id.nav_driver_details)
        }
        binding.valueSeats.text = driver.car?.seats?.minus(ride.rideParticipations.size)?.toString()


        if(!driver.ratings.isNullOrEmpty()){
            driver.ratings.forEach { rating ->
                addComment(
                    binding.rvComments,
                    rating.user.firstName+" "+rating.user.lastName,
                    rating.comment,
                    rating.rating
                    )
            }
        }

        setupRatingSpinner()

        binding.btnSubmitComment.setOnClickListener {
            postComment()
        }

        val btn = activity?.findViewById<ExtendedFloatingActionButton>(R.id.fab)!!

        if(ride.approved) {
            btn.setOnClickListener {
                userViewModel.participateIn()
            }
        }else{
            btn.visibility = View.GONE
        }

        return binding.root

    }





    override fun onMapReady(p0: GoogleMap) {
        val ride = userViewModel.selectedRide.value
        val service = MapServices.getMapServices()
        val distantance = service.geoLocDistance(ride.departure,ride.destination)
        val zoom = service.calculateZoomLevel(distantance)
        val destMarker = MarkerOptions().position(ride.destination.toLatLng())
        val depMarker = MarkerOptions().position(ride.departure.toLatLng())
        p0.moveCamera(CameraUpdateFactory.newLatLngZoom(centerMap(ride),zoom.toFloat()))
        p0.addMarker(destMarker)
        p0.addMarker(depMarker)
    }


    fun centerMap(ride: Ride): LatLng {
        val lat = (ride.departure.latitude + ride.destination.latitude) / 2
        val lon = (ride.departure.longitude + ride.destination.longitude) / 2
        return LatLng(lat, lon)
    }

    fun addComment(
        container: LinearLayout,
        userName: String,
        commentText: String,
        rating: Int,
    ) {
        val context = container.context

        val itemLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }

        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }



        val nameAndDate = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 0, 0)
        }

        val nameText = TextView(context).apply {
            text = userName
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(null, Typeface.BOLD)
        }



        nameAndDate.addView(nameText)

        header.addView(nameAndDate)

        val starsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }

        for (i in 1..5) {
            val star = TextView(context).apply {
                text = "★"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(if (i <= rating) "#FFC107".toColorInt() else Color.LTGRAY)
            }
            starsLayout.addView(star)
        }

        val commentTextView = TextView(context).apply {
            text = commentText
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }

        itemLayout.addView(header)
        itemLayout.addView(starsLayout)
        itemLayout.addView(commentTextView)

        itemLayout.background = AppCompatResources.getDrawable(context,R.drawable.item_bg)

        itemLayout.setPadding(28)


        container.addView(itemLayout)
    }

    fun setupRatingSpinner() {
        val ratingOptions = listOf(
            "5 Stars - Excellent",
            "4 Stars - Very Good",
            "3 Stars - Good",
            "2 Stars - Fair",
            "1 Star - Poor"
        )


        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ratingOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRating.adapter = adapter

        binding.spinnerRating.setSelection(4)
    }


    fun postComment(){
        val text = binding.etComment.text
        if(text.trim().isNullOrEmpty()){
            return
        }
        val rate = 5 - binding.spinnerRating.selectedItemPosition
        userViewModel.addComment(text.trim().toString(),rate)
    }

}