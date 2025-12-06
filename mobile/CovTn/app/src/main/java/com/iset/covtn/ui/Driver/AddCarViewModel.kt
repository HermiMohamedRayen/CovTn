package com.iset.covtn.ui.Driver

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddCarViewModel : ViewModel() {

    // Holds car details
    var carModel: String = ""
    var carMatriculation: String = ""
    var smokerAllowed: Boolean = false
    var airConditioner: Boolean = false

    // LiveData to hold the list of selected image URIs
    private val _selectedImageUris = MutableLiveData<List<Uri>>(emptyList())
    val selectedImageUris: LiveData<List<Uri>> = _selectedImageUris

    /**
     * Adds a list of image URIs to the existing list in LiveData.
     * @param uris The list of URIs to add.
     */
    fun addImageUris(uris: List<Uri>) {
        val currentList = _selectedImageUris.value?.toMutableList() ?: mutableListOf()
        currentList.addAll(uris)
        _selectedImageUris.value = currentList
    }
}