package com.naman14.instantconnect.transfer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.naman14.instantconnect.ProfileActivity
import com.naman14.instantconnect.databinding.ActivityNearbyUsersBinding

/*
Broadcasts self address to nearby devices
and listens for messages from nearby devices containing either their wallet address or alias

only "a:${address}" messages are handled and transmitted in this activity
 */
class NearbyUsersActivity : BaseTransferActivity() {

    private lateinit var binding: ActivityNearbyUsersBinding

    private lateinit var selfAddress: String
    private lateinit var selfAlias: String

    private var sendAddressMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNearbyUsersBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        selfAddress =
            PreferenceManager.getDefaultSharedPreferences(this).getString("address", "") ?: ""
        selfAlias = PreferenceManager.getDefaultSharedPreferences(this).getString("alias", "") ?: ""

        binding.ripple.startRippleAnimation()

        setupTransmitter()
        setupReceiver()

        sendAddressMessage = "a:${if (selfAlias.isEmpty()) selfAddress else selfAlias}"

        send("a:${selfAddress}")
        broadcastAddress()
    }

    private fun broadcastAddress() {
        Handler().postDelayed({
            if (!active) return@postDelayed
            if (sendAddressMessage.isNotEmpty()) {
                send(sendAddressMessage)
            }
            if (active)
                broadcastAddress()
        }, 2000)
    }


    private var foundAddresses = arrayListOf<String>()

    override fun handleMessageReceived(message: String) {
        Log.d("Transfer", "received message: " + message)
        if (message.startsWith("a")) {
            var address = message.split(":")[1]
            Log.d("Transfer", "found user nearby: " + address)

            if (!foundAddresses.contains(address)) {
                foundAddresses.add(address)
            }

            // showing only max 3 nearby users
            foundAddresses.take(3).forEachIndexed { index, s ->
                if (index == 0) {
                    binding.user1.text = address
                    binding.user1.setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            putExtra("address", address)
                        })
                    }
                } else if (index == 1) {
                    binding.user2.text = address
                    binding.user2.setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            putExtra("address", address)
                        })
                    }
                } else if (index == 2) {
                    binding.user3.text = address
                    binding.user3.setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            putExtra("address", address)
                        })
                    }
                }
            }

            if (foundAddresses.isNotEmpty()) {
                binding.foundUserContainer.isVisible = true
                binding.tvFoundUser.text = "Found ${foundAddresses.size} nearby users"
            } else {
                binding.foundUserContainer.isVisible = false
            }
        }
    }

}