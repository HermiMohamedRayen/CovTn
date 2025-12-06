package com.iset.covtn.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iset.covtn.localDatabase.Localatabase
import com.iset.covtn.localDatabase.repository.Repository
import com.iset.covtn.models.AuthObj
import com.iset.covtn.models.User
import com.iset.covtn.services.AuthServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext


class AuthViewModel(private val repository: Repository) : ViewModel() {


    private val authServices = AuthServices.getAuthService()
    val user = repository.getUser();
    private val _authentified = MutableLiveData<Boolean>(null)
    val authentified: LiveData<Boolean> = _authentified

    private val _authObj = MutableLiveData<AuthObj>()
    val authObj : LiveData<AuthObj> = _authObj

    fun authentify() {
        viewModelScope.launch {
            val localuser = repository.getMe()
            if (localuser != null) {
                Log.i("ff", "authentify: ${localuser.token}")
                val user = authServices.authentify(localuser.token)
                if (user != null) {
                    Log.i("TAG", "authentify: ok")
                    user.token = localuser.token
                    repository.insertUser(user)
                    _authentified.value = true
                    return@launch

                }
            }
            _authentified.value = false
        }

    }

    fun login(email : String , pass : String){
        viewModelScope.launch {
            val obj = withContext(Dispatchers.IO){
                authServices.login(email,pass)
            }
            _authObj.value = obj?: AuthObj("","","404",null)
        }
    }

    fun verifyMail(obj : AuthObj){
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO){authServices.verifyMail(obj)}
            if(res) {
                val usr = authServices.authentify(obj.token!!)
                usr?.token = obj.token!!
                repository.insertUser(usr!!)
                _authentified.value = true
            }else{
                _authentified.value = false
            }
        }
    }

    fun signIn(user : User){
        viewModelScope.launch {
            val obj = withContext(Dispatchers.IO){
                authServices.signin(user)
            }
            _authObj.value = obj
        }
    }

}