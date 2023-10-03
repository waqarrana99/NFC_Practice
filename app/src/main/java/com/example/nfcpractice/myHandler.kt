package com.example.nfcpractice

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.nfcpractice.databinding.ItemPopUpBinding

class myHandler {
    companion object{
        fun FragmentActivity.Popup() {
            val binding = ItemPopUpBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setView(binding.root)
            val dialog = builder.create()
            dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_background)
            dialog.show()

            FinalKeys.k_Update.observe(this){
                dialog.dismiss()
            }

        }

    }
}