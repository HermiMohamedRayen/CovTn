package com.iset.covtn.ui.login

import android.app.Application
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.iset.covtn.CovTnApplication
import com.iset.covtn.services.AuthServices
import com.iset.covtn.HomeActivity
import com.iset.covtn.databinding.ActivityLoadingBinding
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.localDatabase.Localatabase
import com.iset.covtn.databinding.ActivityVerifyMailBinding
import com.iset.covtn.localDatabase.repository.Repository
import com.iset.covtn.models.AuthObj
import com.iset.covtn.ui.viewModel.AuthViewModel
import com.iset.covtn.ui.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class VerifyMailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyMailBinding;
    private lateinit var daoUser: DaoUser;

    private lateinit var loading: ActivityLoadingBinding

    private val authViewModel : AuthViewModel by viewModels {
        val app = application as CovTnApplication
        val repo = app.repository
        UserViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyMailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = ActivityLoadingBinding.inflate(layoutInflater)

        authViewModel.authentified.observe(this){
            value ->
                if(value == true) {
                    Toast.makeText(applicationContext, "connction success", Toast.LENGTH_LONG).show()
                    val int = Intent(applicationContext, HomeActivity::class.java)
                    int.setAction(Intent.ACTION_MAIN)
                    startActivity(int)
                    finish()
                }

                else if(value!=null)  {
                    setContentView(binding.root)
                    Toast.makeText(applicationContext,"invalid code", Toast.LENGTH_LONG).show()

                }

        }

        val obj: AuthObj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("authobj", AuthObj::class.java)
        } else {
            @Suppress("Deprecation")
            intent.getParcelableExtra("authobj")
        } ?: error("AuthObj not found in Intent")

        daoUser = Localatabase.getDatabase(this).getUserDao()

        binding.verify.setOnClickListener {

            obj.code = binding.inpmsg.text.toString().trim()

            authViewModel.verifyMail(obj)

        }
    }

}