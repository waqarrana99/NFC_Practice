package com.example.nfcpractice

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.nfcpractice.databinding.ActivityMainBinding
import com.example.nfcpractice.databinding.ItemPopUpBinding
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var writeCheck = false
    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnWrite.setOnClickListener {
            if (binding.editText.text.isNotEmpty()){
                Popup()
            }else{
                binding.editText.error="Add something!"
            }

        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (writeCheck) {
            writeCheck = false
            if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
                // Handle the NFC tag here
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                // Extract data from your input field
                val dataToWrite = binding.editText.text.toString()
                // Write data to the NFC tag
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    try {
                        ndef.connect()
                        val ndefMessage =
                            NdefMessage(NdefRecord.createTextRecord(null, dataToWrite))
                        ndef.writeNdefMessage(ndefMessage)
                        Toast.makeText(this, "Written to the NFC tag", Toast.LENGTH_SHORT).show()
                        // Data has been written to the NFC tag
                    } catch (e: IOException) {
                        e.printStackTrace()
                        // Handle exceptions
                        Log.d("waqar", "IOException TOP: ")
                    } catch (e: FormatException) {
                        e.printStackTrace()
                        Log.d("waqar", "FormatException: ")
                    } finally {
                        try {
                            ndef.close()
                            Log.d("waqar", "onNewIntent: ")
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Log.d("waqar", "IOException BOTTOM: ")
                        }
                    }
                } else {
                    Toast.makeText(this, "NDEF is not writable...", Toast.LENGTH_SHORT).show()
                    // NFC tag is not NDEF formatted or not writable
                    // Handle this case
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val intentFilters = arrayOf(tagDetected)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }


    override fun onPause() {
        super.onPause()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableForegroundDispatch(this)
    }

    fun FragmentActivity.Popup() {
        val binding = ItemPopUpBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_background)
        dialog.show()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            // Handle the scenario where NFC is not available on this device
            dialog.dismiss()
            Toast.makeText(this, "NFC Not supported", Toast.LENGTH_SHORT).show()

        } else {
            // Create a PendingIntent object so the detected NFC tag is sent to this activity
            Toast.makeText(this, "SCAN CARD...", Toast.LENGTH_SHORT).show()
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE
            )
            writeCheck=true

            val handler = Handler()
            val delayMillis: Long = 9000 // 9 seconds in milliseconds

            handler.postDelayed({
                runOnUiThread {
                    writeCheck=false
                    dialog.dismiss()
                }
            }, delayMillis)
        }

    }


}