package com.example.nfcpractice.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import java.io.IOException

class TagWriter @Throws(FormatException::class) constructor(tag: Tag) {

    private val NDEF = Ndef::class.java.canonicalName
    private val NDEF_FORMATABLE = NdefFormatable::class.java.canonicalName

    private val ndef: Ndef?
    private val ndefFormatable: NdefFormatable?

    val tagId: String?
        get() {
            if (ndef != null) {
                return bytesToHexString(ndef.tag.id)
            } else if (ndefFormatable != null) {
                return bytesToHexString(ndefFormatable.tag.id)
            }
            return null
        }

    init {
        val technologies = tag.techList
        val tagTechs = listOf(*technologies)
        when {
            tagTechs.contains(NDEF) -> {
                Log.i("waqar", "WritableTag contains ndef")
                ndef = Ndef.get(tag)
                ndefFormatable = null
            }
            tagTechs.contains(NDEF_FORMATABLE) -> {
                Log.i("waqar", "WritableTag contains ndef_format_able")
                ndefFormatable = NdefFormatable.get(tag)
                ndef = null
            }
            else -> {
                throw FormatException("Tag doesn't support ndef")
            }
        }
    }

    @Throws(IOException::class, FormatException::class)
    fun writeData(
        tagId: String,
        message: NdefMessage
    ): Boolean {
        if (tagId != tagId) {
            return false
        }
        if (ndef != null) {
            try {
                ndef.connect()
                if (ndef.isConnected) {
                    ndef.writeNdefMessage(message)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (ndefFormatable != null) {
            ndefFormatable.connect()
            if (ndefFormatable.isConnected) {
                ndefFormatable.format(message)
                return true
            }
        }
        return false
    }

    fun isWritable(sectorNumber: Int): Boolean {
        if (ndef != null) {
            return try {
                ndef.connect()
                val maxSize = ndef.maxSize
                ndef.close()
                // Assume that each sector can store up to 16 characters (adjust as per your card's specifications)
                val maxCharactersPerSector = maxSize / 16
                sectorNumber < maxCharactersPerSector
            } catch (e: IOException) {
                false
            }
        } else if (ndefFormatable != null) {
            return try {
                ndefFormatable.connect()
                ndefFormatable.isConnected
            } catch (e: IOException) {
                false
            }
        }
        return false
    }

    @Throws(IOException::class)
    private fun close() {
        ndef?.close() ?: ndefFormatable?.close()
    }

    companion object {
        fun bytesToHexString(src: ByteArray): String? {
            if (ByteCheck.isNullOrEmpty(src)) {
                return null
            }
            val sb = StringBuilder()
            for (b in src) {
                sb.append(String.format("%02X", b))
            }
            return sb.toString()
        }
    }

}