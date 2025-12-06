package com.iset.covtn.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentDriverProfileBinding
import com.iset.covtn.models.User
import com.iset.covtn.ui.viewModel.UserViewModel
import java.io.File

class DriverProfileFragment : Fragment() {


    private lateinit var binding : FragmentDriverProfileBinding

    private val userViewModel : UserViewModel by activityViewModels()

    private val carPhotos = mutableListOf<MutableLiveData<File>>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDriverProfileBinding.inflate(inflater,container,false)

        val image = MutableLiveData<File>()
        val ride = userViewModel.selectedRide.value!!
        val driver = ride.driver

        image.observe(viewLifecycleOwner){
            value -> binding.profileImage.setImageBitmap(BitmapFactory.decodeFile(value.absolutePath) )
        }

        userViewModel.getUserImage(requireContext(),driver.profilePicture,driver.email,image)

        binding.tvFullName.text = "${driver.firstName} ${driver.lastName}"
        binding.tvPhone.text = driver.number
        binding.tvEmail.text = driver.email
        binding.tvAirConditioned.text = if(driver.car?.airConditioner!!){
            "have Air Conditioner"
        }else{
            "doesn't have Air Conditioned"
        }

        binding.tvSmoker.text = if(driver.car?.smoker!!){
            "Smoker are allowed"
        }else{
            "Smoker are not allowed"
        }

        binding.tvMatriculation.text = driver.car?.matriculationNumber
        binding.tvCarModel.text = driver.car?.model

        driver.car?.photos?.forEach {
            val ob = MutableLiveData<File>()
            ob.observe(viewLifecycleOwner){
                value ->
                val img = ImageView(requireContext())
                val widthInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
                val heightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
                val la = LinearLayout.LayoutParams(widthInPx,heightInPx)
                la.marginEnd = 35
                img.layoutParams = la
                img.setImageBitmap(BitmapFactory.decodeFile(value.absolutePath) )
                binding.carImages.addView(img)
            }
            userViewModel.getCarImage(requireContext(),it,driver.email,ob)
            carPhotos.add(ob)

        }
        var rate = 0.0f;

        if(ride.driver.ratings?.size?:0 > 0){
            ride.driver.ratings?.forEach {
                rate += it.rating
            }
            binding.tvRating.text = "rating : ${rate/ride.driver.ratings?.size!!}"
        }else{
            binding.tvRating.text = "rating : 0"
        }

        binding.tvRating.text

        return binding.root
    }

}