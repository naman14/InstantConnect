package com.naman14.instantconnect.transfer

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.quietmodem.Quiet.FrameTransmitter
import org.quietmodem.Quiet.FrameTransmitterConfig
import org.quietmodem.Quiet.ModemException
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.Subscriptions
import java.io.IOException
import java.nio.charset.Charset

/*
Base class for using data transfer using sound
exposes common function to setup receiver and transmitter and to handle incoming message and sening messages
 */
open class BaseTransferActivity: AppCompatActivity() {

    private val REQUEST_AUDIO_CODE = 1

    public var frameSubscription: Subscription = Subscriptions.empty()
    public var transmitter: FrameTransmitter? = null

    public var active = true

    // tested many different profile, this is the one that gave best results, keeping in mind the speed and frame length
    // this profile can transfer around 16 characters in 1 frame
    private val PROFILE = "ultrasonic-fsk-fast"

    private fun toggleToSender() {
        Log.d("Transfer", "toggling to sender")
        frameSubscription.unsubscribe()
        if (transmitter != null) {
            transmitter!!.close()
        }

        setupTransmitter()
    }

    private fun toggleToReceiver() {
        Log.d("Transfer", "toggling to receiver")
        frameSubscription.unsubscribe()
        if (transmitter != null) {
            transmitter!!.close()
        }

        setupReceiver()
    }


    /*
    send payload to nearby devices
     */
    public fun send(payload: String) {

        Log.d("Transfer", "Send: " + payload)
        if (transmitter == null) {
            setupTransmitter()
        }

        try {

            if (payload.length > 15) {
                val chunked = payload.chunked(15)
                chunked.forEach {
                    var pendingMarker = "-"
                    if (chunked.last() == it) {
                        pendingMarker = ""
                    }
                    Log.d("Transfer", "send: " + (it + pendingMarker))
                    transmitter?.send((it + pendingMarker).toByteArray())
                }
            } else {
                transmitter?.send(payload.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Send failed. Try again", Toast.LENGTH_SHORT).show()
            // our message might be too long or the transmit queue full
        }
    }


    override fun onResume() {
        super.onResume()
        active = true
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
        if (transmitter != null) {
            transmitter!!.close()
        }
        frameSubscription.unsubscribe()
        if (transmitter != null) {
            transmitter!!.close()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        active = false
        if (transmitter != null) {
            transmitter!!.close()
        }
        frameSubscription.unsubscribe()
        if (transmitter != null) {
            transmitter!!.close()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_AUDIO_CODE -> {

                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    subscribeToFrames()
                } else {
                    showMissingAudioPermissionToast()
                    finish()
                }
            }
        }
    }

    public fun setupTransmitter() {
        val transmitterConfig: FrameTransmitterConfig
        try {
            transmitterConfig = FrameTransmitterConfig(
                this, PROFILE
            )
            transmitter = FrameTransmitter(transmitterConfig)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: ModemException) {
            throw RuntimeException(e)
        }
    }


    /*
    setup receiver to listen for incoming frames, audio permission is necessary
     */
    public fun setupReceiver() {
        if (hasRecordAudioPersmission()) {
            subscribeToFrames()
        } else {
            requestPermission()
        }
    }

    public fun subscribeToFrames() {
        Log.d("Transfer", "Subsrcibe to frames")
        frameSubscription.unsubscribe()
        frameSubscription =
            FrameReceiverObservable.create(this, PROFILE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    { buf: ByteArray ->
                        val message = String(buf, Charset.forName("UTF-8"))
                        Log.d("AudioReceive", message)
                        val time = System.currentTimeMillis() / 1000
                        val timestamp = time.toString()
                        Log.d("AudioReceive", "Received " + buf.size + " @" + timestamp)
                        handleMessageReceived(message)
                    }
                ) { error: Throwable ->  Log.d("AudioReceiveError", "Error") }
    }

    private fun hasRecordAudioPersmission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_CODE
        )
    }

    private fun showMissingAudioPermissionToast() {
        val context: Context = applicationContext
        val duration: Int = Toast.LENGTH_SHORT
        val toast: Toast = Toast.makeText(context, "Audio permission is required", duration)
        toast.show()
    }

    /*
    inherited clases need to override this to handle incoming messages
     */
    public open fun handleMessageReceived(message: String) {

    }

}