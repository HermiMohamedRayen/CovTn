package com.iset.covtn.ui.Driver

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.gson.Gson
import com.iset.covtn.R
import com.iset.covtn.databinding.FragmentAddCarBinding
import com.iset.covtn.models.Car
import com.iset.covtn.services.UserService
import com.iset.covtn.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddCarFragment : Fragment() {

    private var _binding: FragmentAddCarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddCarViewModel
    private val userViewModel: UserViewModel by activityViewModels()

    private var token = ""



    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uris = mutableListOf<Uri>()

                result.data?.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        uris.add(clip.getItemAt(i).uri)
                    }
                } ?: result.data?.data?.let { singleUri ->
                    uris.add(singleUri)
                }

                viewModel.addImageUris(uris)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCarBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AddCarViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"))
        }

        binding.saveCarButton.setOnClickListener {
            it.isEnabled = false
            saveCarDetails()
        }

        viewModel.selectedImageUris.observe(viewLifecycleOwner) { uris ->
            updateImagePreviews(uris)
        }

        binding.seatsPicker.minValue = 1
        binding.seatsPicker.maxValue = 10
        binding.seatsPicker.value = 4 // Default value

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            token = user.token
        }

        userViewModel.user.observe(viewLifecycleOwner){
                value -> if (value.car != null) {
                findNavController().navigate(R.id.propose_ride, null,
                    navOptions {
                        popUpTo (R.id.nav_add_car) { inclusive = true }
                    }
                )

            }


        }
    }

    private fun saveCarDetails() {
        val ctx = context ?: return

        val model = binding.carModelEditText.text.toString()
        val matriculation = binding.carMatriculationEditText.text.toString()
        val smoker = binding.smokerAllowedSwitch.isChecked
        val ac = binding.airConditionerSwitch.isChecked
        val images = viewModel.selectedImageUris.value ?: emptyList()
        val seats = binding.seatsPicker.value // Get value from NumberPicker

        if (model.isBlank() || matriculation.isBlank()) {
            Toast.makeText(ctx, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            binding.saveCarButton.isEnabled = true
            return
        }

        if (images.size < 2) {
            Toast.makeText(ctx, "Select at least 2 images", Toast.LENGTH_SHORT).show()
            binding.saveCarButton.isEnabled = true
            return
        }

        if (token.isNullOrBlank()) {
            Toast.makeText(ctx, "User not logged in", Toast.LENGTH_SHORT).show()
            binding.saveCarButton.isEnabled = true
            return
        }

        // Fix parameter order of Car object
        val car = Car(
            id = 0,
            matriculationNumber = matriculation,
            model = model,
            photos = ArrayList(),
            airConditioner = ac,
            smoker = smoker,
            seats = seats,
        )

        val carJson = Gson().toJson(car)
        val carRequestBody = carJson.toRequestBody("application/json".toMediaType())

        val filesParts = images.mapNotNull { uri ->
            ctx.contentResolver?.let { resolver ->
                val file = getFileFromUri(resolver, uri)
                val body = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("files", file.name, body)
            }
        }

        userViewModel.addCar(token, carRequestBody, filesParts)
        userViewModel.success.observe(viewLifecycleOwner) { success ->
            if (success != null) {
                findNavController().popBackStack(R.id.nav_home, false)
                userViewModel.success.removeObservers(viewLifecycleOwner)
            }
            }



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

    private fun updateImagePreviews(uris: List<Uri>) {
        val ctx = context ?: return
        binding.imagePreviewsContainer.removeAllViews()

        for (uri in uris) {
            val img = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 0, 8, 0)
                }
                setImageURI(uri)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            binding.imagePreviewsContainer.addView(img)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
