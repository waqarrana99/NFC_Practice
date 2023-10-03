package com.example.nfcpractice.nfc

class ByteCheck {
    companion object {
        fun isNullOrEmpty(array: ByteArray?): Boolean {
            if (array == null) {
                return true
            }
            val length = array.size
            for (i in 0 until length) {
                if (array[i].toInt() != 0) {
                    return false
                }
            }
            return true
        }
    }
}