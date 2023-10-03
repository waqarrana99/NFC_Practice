package com.example.nfcpractice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nfcpractice.databinding.ActivityMainBinding
import com.example.nfcpractice.myHandler.Companion.Popup
import com.example.nfcpractice.nfc.TagWriter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    var tag: TagWriter? = null
    var tagId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initNfcAdapter()


        binding.btnWrite.setOnClickListener {
            if (binding.editText.text.isNotEmpty()) {
                if (checkNFCDeviceSupport()) {
                    Popup()
                    FinalKeys.popUp_Check = true
                }
            } else {
                binding.editText.error = "Add something!"
            }

        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val tagFromIntent = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        try {
            tag = TagWriter(tagFromIntent!!)
        } catch (e: FormatException) {
            Log.e("PodsActivity", "Unsupported tag tapped", e)
            return
        }
        tagId = tag!!.tagId

        if (FinalKeys.popUp_Check) {
            writeMessage()
            FinalKeys.popUp_Check = false
        }

    }

//    private fun writeMessage() {
//        Log.e("waqar", "writeMessage: ${binding.editText.text.toString()}")
//
//        val txt = binding.editText.text.toString()
//
//        val record: NdefRecord =
//            NdefRecord.createUri(Uri.parse(txt))
//        val msg = NdefMessage(arrayOf(record))
//
//        val writeResult = tag?.writeData(tagId!!, msg)
//
//        if (writeResult != null) {
//            FinalKeys.k_Update.value= "kjshfkjahkjahdakjds"
//            Toast.makeText(this@MainActivity, "Tag Write Success!", Toast.LENGTH_SHORT).show()
//        } else {
//            FinalKeys.k_Update.value= "kjshfkjahkjahdakjds"
//            Toast.makeText(this@MainActivity, "Tag Write Failed!", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun writeMessage() {
        val textToWrite = binding.editText.text.toString()
        val uri = Uri.parse(textToWrite)
        val records: MutableList<NdefRecord> = ArrayList()

        // Divide the text into 16-character chunks and create NdefRecord for each chunk
        var remainingText = textToWrite
        while (remainingText.isNotEmpty()) {
            val chunk = if (remainingText.length > 16) {
                remainingText.substring(0, 16)
            } else {
                remainingText
            }
            remainingText = if (remainingText.length > 16) {
                remainingText.substring(16)
            } else ""

            // Create a URI NdefRecord for the chunk
            val record = NdefRecord.createUri(uri)
            records.add(record)
        }

        // Write records to the writable sectors (9, 10, 12, 13, 14)
        for (sectorNumber in arrayOf(9, 10, 12, 13, 14)) {
            if (tag?.isWritable(sectorNumber) == true) {
                val msg = NdefMessage(records.toTypedArray())
                val writeResult = tag?.writeData(sectorNumber.toString(), msg)
                if (writeResult != null) {
                    // Write successful
                    FinalKeys.k_Update.value = "kjshfkjahkjahdakjds"
                    Toast.makeText(this@MainActivity, "Tag Write Success!", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
            }
        }

        // If no writable sectors were found or write failed
        FinalKeys.k_Update.value = "kjshfkjahkjahdakjds"
        Toast.makeText(this@MainActivity, "Tag Write Failed!", Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        if (checkNFCDeviceSupport()) {
            enableNfcForegroundDispatch()
        }
        super.onResume()
    }

    override fun onPause() {
        if (checkNFCDeviceSupport()) {
            disableNfcForegroundDispatch()
        }
        super.onPause()
    }


    private fun initNfcAdapter() {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        nfcAdapter = nfcManager.defaultAdapter
    }

    private fun checkNFCDeviceSupport(): Boolean {
        if (nfcAdapter == null) {
            Toast.makeText(this@MainActivity, "NFC not supported", Toast.LENGTH_SHORT).show()
            return false
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this@MainActivity, "Turn ON your NFC", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_MUTABLE
            )
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e("PodsActivity", "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e("PodsActivity", "Error disabling NFC foreground dispatch", ex)
        }
    }

}