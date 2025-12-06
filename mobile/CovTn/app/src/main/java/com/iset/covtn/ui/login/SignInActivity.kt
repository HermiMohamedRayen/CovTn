package com.iset.covtn.ui.login

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.iset.covtn.CovTnApplication
import com.iset.covtn.databinding.ActivityLoadingBinding
import com.iset.covtn.services.AuthServices
import com.iset.covtn.databinding.ActivitySignInBinding
import com.iset.covtn.models.User
import com.iset.covtn.ui.viewModel.AuthViewModel
import com.iset.covtn.ui.viewModel.UserViewModelFactory

class SignInActivity : AppCompatActivity() {

    private lateinit var loading : ActivityLoadingBinding
    private val authViewModel : AuthViewModel by viewModels {
        val app = application as CovTnApplication
        val repo = app.repository
        UserViewModelFactory(repo)
    }
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)

        loading = ActivityLoadingBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.changer.setOnClickListener {
            goToLogin()
        }

        binding.btnsignin.setOnClickListener {
            performSignin()
        }
        authViewModel.authObj.observe(this){
                value -> if(value!=null){
            if(!value.code.isNullOrEmpty() && value.code.toInt() in 400..500){
                Toast.makeText(this,value.email, Toast.LENGTH_LONG).show()
            }
            else{
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

    }

    fun goToLogin() {
        val int = Intent(this, LoginActivity::class.java)
        int.setAction(Intent.ACTION_MAIN)
        startActivity(int)
        finish()
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return emailRegex.matches(email)
    }

    fun isValidName(name: String): Boolean {
        val nameregex = "[a-zA-Z]{2}+[a-zA-Z ]+".toRegex()
        return nameregex.matches(name)
    }

    fun performSignin() {
        val firstname = binding.inpFname.text.toString().trim()
        val lastname = binding.inpLname.text.toString().trim()
        val pass = binding.inppass.text.toString()
        val email = binding.impmail.text.toString()
        val repass = binding.inppassretype.text.toString()
        if (!isValidName(firstname)) {
            Toast.makeText(this, "invalid first name", Toast.LENGTH_LONG).show()
            return
        } else if (!isValidName(lastname)) {
            Toast.makeText(this, "invalid last name", Toast.LENGTH_LONG).show()
            return
        } else if (!isValidEmail(email)) {
            Toast.makeText(this, "invalid email", Toast.LENGTH_LONG).show()
            return
        } else if (pass.length < 8) {
            Toast.makeText(this, "minimun password length is 8", Toast.LENGTH_LONG).show()
            return
        } else if (repass != pass) {
            Toast.makeText(this, "passwords does not match", Toast.LENGTH_LONG).show()
            return
        }
        setContentView(loading.root)
        val user = User(email, firstname, lastname, pass, null,null,"")
        authViewModel.signIn(user)


    }
}