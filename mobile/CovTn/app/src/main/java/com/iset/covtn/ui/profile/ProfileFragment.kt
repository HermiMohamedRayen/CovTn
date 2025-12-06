package com.iset.covtn.ui.profile

import android.app.AlertDialog
import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iset.covtn.R
import com.iset.covtn.databinding.ActivityProfileBinding
import com.iset.covtn.models.GeoLocation
import com.iset.covtn.models.Ride
import com.iset.covtn.models.User
import com.iset.covtn.ui.login.LoginActivity
import com.iset.covtn.ui.viewModel.UserViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class ProfileFragment : Fragment() {
    private lateinit var binding: ActivityProfileBinding;


    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            val file = requireContext().contentResolver?.let { resolver ->
                val profileFile = getFileFromUri(resolver, uri)
                val body = profileFile.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("file", profileFile.name, body)
            }

            if (file != null) viewModel.updateProfilePicture(file)
        }


    private val viewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { value ->
            binding.progressBar.visibility = View.GONE
            if (value != null) {
                fillView(value)
                if (value.car != null) {
                    binding.btnDriverDetails.visibility = View.VISIBLE
                    binding.btnDriverDetails.setOnClickListener {
                        val ride = Ride(
                            id = 0,
                            driver = value,
                            departure = GeoLocation(0.0, 0.0),
                            destination = GeoLocation(0.0, 0.0),
                            departureTime = Date(),
                            arrivalTime = Date(),
                            approved = false,
                            rideParticipations = ArrayList(),

                        )
                        viewModel.selectRide(ride)
                        findNavController().navigate(R.id.action_profile_to_nav_driver_details)
                    }
                }
            } else {
                binding.tvEmail.text = "loading ..."
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        viewModel.image.observe(viewLifecycleOwner) { value ->
            if (value != null) {
                binding.profileImage.setImageBitmap(BitmapFactory.decodeFile(value.absolutePath))
            }
        }
        viewModel.getImage(requireContext())

        binding.profileImage.setOnClickListener {
            val d = AlertDialog.Builder(requireContext())
            d.setTitle("Change Profile Picture")
            d.setMessage("Do you want to change your profile picture?")
            d.setPositiveButton("Yes") { dialog, _ ->
                imagePicker.launch("image/*")
                dialog.dismiss()
            }
            d.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            d.show()

        }

        binding.Phone.setOnLongClickListener {
            showUpdatePhoneNumberDialog()
            true
        }


    }

    private fun fillView(user: User) {
        binding.tvEmail.text = user.email
        binding.tvFirstname.text = user.firstName
        binding.tvLastname.text = user.lastName
        binding.tvPhone.text = user.number


    }

    private fun showUpdatePhoneNumberDialog() {
        // --- Create the EditText programmatically ---
        val editText = EditText(requireContext()).apply {
            // Set current phone number as the text
            setText(binding.tvPhone.text)
            // Set the input type to phone
            inputType = InputType.TYPE_CLASS_PHONE
            // Set a hint
            hint = "New Phone Number"
        }

        // --- Create a container for the EditText to add some padding ---
        // The AlertDialog by default has no padding for a custom view.
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Add horizontal and vertical margins for padding
        params.setMargins(48, 0, 48, 0)
        editText.layoutParams = params
        container.addView(editText)

        // --- Build and show the dialog ---
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Phone Number")
            .setView(container) // Set the container with the EditText
            .setPositiveButton("Update") { dialog, _ ->
                val newPhoneNumber = editText.text.toString()
                if (newPhoneNumber.isNotBlank()) {
                    viewModel.updatePhoneNumber(newPhoneNumber)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun getFileFromUri(resolver: ContentResolver, uri: Uri): File {
        val fileName = resolver.getFileName(uri)
        val file = File(requireContext().cacheDir, fileName)
        file.createNewFile()

        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }
    private fun ContentResolver.getFileName(uri: Uri): String {
        var name = "file_${System.currentTimeMillis()}"
        val cursor = query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }
}