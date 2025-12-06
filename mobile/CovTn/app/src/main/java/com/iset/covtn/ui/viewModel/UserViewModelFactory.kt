package com.iset.covtn.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iset.covtn.localDatabase.repository.Repository

class UserViewModelFactory(
    val repository: Repository
) : ViewModelProvider.Factory {



    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UserViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }

        throw IllegalArgumentException("unknown class")
    }

}