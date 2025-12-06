package com.iset.covtn.ui.login

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.iset.covtn.CovTnApplication
import com.iset.covtn.services.AuthServices
import com.iset.covtn.HomeActivity
import com.iset.covtn.databinding.ActivityLoadingBinding
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.localDatabase.Localatabase
import com.iset.covtn.databinding.LoginActivityBinding
import com.iset.covtn.localDatabase.repository.Repository
import com.iset.covtn.models.AuthObj
import com.iset.covtn.ui.viewModel.AuthViewModel
import com.iset.covtn.ui.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class LoginActivity : AppCompatActivity() {
    private lateinit var binding : LoginActivityBinding
    private val authViewModel : AuthViewModel by viewModels {
        val app = application as CovTnApplication
        val repo = app.repository
        UserViewModelFactory(repo)
    }

    private lateinit var loading : ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)

        loading = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(loading.root)


        authViewModel.authentified.observe(this){
            value -> if (value==true){
                val int = Intent(applicationContext, HomeActivity::class.java)
                int.setAction(Intent.ACTION_MAIN)
                startActivity(int)
                finish()
            }else if(value!=null){
                setContentView(binding.root)
            }
        }

       authViewModel.authentify()

        authViewModel.authObj.observe(this){
            value -> if(value!=null){
                if(value.code=="404"){
                    Toast.makeText(this,"email ou mot de pass incorrect", Toast.LENGTH_LONG).show()
                }else{
                    val int = Intent(this, VerifyMailActivity::class.java)
                    int.setAction(Intent.ACTION_MAIN)
                    int.putExtra("authobj",value)
                    startActivity(int)
                    finish()
                    return@observe
                }
            setContentView(binding.root)
        }
        }

        binding.changer.setOnClickListener {
            val int = Intent(this, SignInActivity::class.java)
            int.setAction(Intent.ACTION_MAIN)
            startActivity(int)
            finish()
        }
        binding.btnlogin.setOnClickListener {
            performLogin()
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return emailRegex.matches(email)
    }
    fun performLogin(){
        val pass = binding.inppass.text.toString().trim()
        val email = binding.impmail.text.toString().trim()
        if(!isValidEmail(email)){
            Toast.makeText(this,"invalid email", Toast.LENGTH_LONG).show()
            return
        }
        else if(pass.length < 8){
            Toast.makeText(this,"minimun password length is 8", Toast.LENGTH_LONG).show()
            return
        }

        setContentView(loading.root)
        authViewModel.login(email,pass)


    }


}