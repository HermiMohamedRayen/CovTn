package com.iset.covtn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews
import androidx.lifecycle.ViewModelProvider
import com.iset.covtn.databinding.ActivityHomeBinding
import com.iset.covtn.localDatabase.Localatabase
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.localDatabase.repository.Repository
import com.iset.covtn.models.AuthObj
import com.iset.covtn.services.NotificationSSe
import com.iset.covtn.ui.login.LoginActivity
import com.iset.covtn.ui.viewModel.UserViewModel
import com.iset.covtn.ui.viewModel.UserViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    lateinit var userViewModel: UserViewModel;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = Localatabase.getDatabase(this).getUserDao()

        val repo = Repository(dao)

        val factory = UserViewModelFactory(repo)

        userViewModel = ViewModelProvider.create(this,factory).get(UserViewModel::class.java)


        createNotificationChannel()


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)

        val floatingbtn = binding.appBarHome.fab
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,  R.id.nav_search_ride, R.id.nav_my_participation, R.id.propose_ride
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


            val img = navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView)
            img.setOnClickListener {

                navController.navigate(R.id.profile)
            }

            val name = navView.getHeaderView(0).findViewById<android.widget.TextView>(R.id.name)
            val email = navView.getHeaderView(0).findViewById<android.widget.TextView>(R.id.email)

        userViewModel.user.observe(this){
            value -> if(value!=null){
                name.text = value.firstName
                email.text = value.email
            }
        }

        userViewModel.image.observe(this){
            value -> if(value!=null){
                val image = BitmapFactory.decodeFile(value.absolutePath)
                img.setImageBitmap(image)
            }
        }
        userViewModel.getImage(this)
        floatingbtn.visibility = View.GONE


        navController.addOnDestinationChangedListener { _, dest, _ ->
            if (drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.closeDrawer(navView)
            }

            userViewModel.refreshUserData()

            floatingbtn.visibility = View.GONE
            floatingbtn.setOnClickListener(null)
            floatingbtn.icon = null

            when (dest.id){

                R.id.rideDetails -> {
                    floatingbtn.visibility = View.VISIBLE
                    floatingbtn.text = "book now"
                }

                R.id.nav_search_ride -> {
                    floatingbtn.visibility = View.VISIBLE
                    floatingbtn.text = "set departure"
                }

                R.id.propose_ride -> {
                    floatingbtn.visibility = View.VISIBLE
                    floatingbtn.text = "set departure"
                }



        }


        }


        userViewModel.errormsg.observe(this){
            value -> if(!value.isNullOrEmpty()){
            Snackbar.make(binding.root,value, Snackbar.LENGTH_LONG).setTextColor(Color.RED).show()
            }
        }
        userViewModel.success.observe(this){
                value -> if(!value.isNullOrEmpty()){
            Snackbar.make(binding.root,value, Snackbar.LENGTH_LONG).setTextColor(Color.BLUE).show()
        }
        }

        userViewModel.startSSE(applicationContext)



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        val logoutItem = menu.findItem(R.id.logout_action)

        // Change the text color
        val spanString = SpannableString(logoutItem.title)
        spanString.setSpan(
            ForegroundColorSpan(Color.RED),
            0,
            spanString.length,
            0
        )
        logoutItem.title = spanString
        logout(menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun logout(menu: Menu){
        menu.findItem(R.id.logout_action).setOnMenuItemClickListener {
            val int = Intent(this, LoginActivity::class.java)
            int.setAction(Intent.ACTION_MAIN)
            userViewModel.logout()
            startActivity(int)
            finish()
            true
        }
    }





    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sse_channel",
                "SSE Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Shows notifications"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }



}