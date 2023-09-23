package com.naman14.instantconnect

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import com.naman14.instantconnect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val connectedWalletAddress = PreferenceManager.getDefaultSharedPreferences(this).getString("address", "") ?: ""
//        if (!connectedWalletAddress.equals("")) {
//            startActivity(Intent(this, ProfileActivity::class.java).apply {
//                putExtra("address", connectedWalletAddress)
//            })
//            finish()
//            return
//        }

        startActivity(Intent(this, ProfileActivity::class.java).apply {
            putExtra("address", "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045")
        })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}