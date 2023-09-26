package com.seuic.uhfdemo

import java.util.Locale

object BaseUtil {
    /**
     * Copy the array at the specified location
     */
    fun memcpy(bytesTo: ByteArray, bytesFrom: ByteArray, len: Int) {
        memcpy(bytesTo, 0, bytesFrom, 0, len)
    }

    /**
     *
     */
    fun memcpy(
        bytesTo: ByteArray,
        startIndexTo: Int,
        bytesFrom: ByteArray,
        startIndexFrom: Int,
        len: Int
    ) {
        var startIndexTo = startIndexTo
        var startIndexFrom = startIndexFrom
        var len = len
        while (len-- > 0) {
            bytesTo[startIndexTo++] = bytesFrom[startIndexFrom++]
        }
    }

    /**
     * Copy the array from the start
     */
    fun memcmp(bytes1: ByteArray, bytes2: ByteArray, len: Int): Boolean {
        var len = len
        if (len < 1) {
            return false
        }
        var startIndex = 0
        while (len-- > 0) {
            if (bytes1[startIndex] != bytes2[startIndex]) {
                return false
            }
            startIndex++
        }
        return true
    }

    /**
     * byte array to hexadecimal string
     */
    fun getHexString(b: ByteArray, length: Int): String {
        return getHexString(b, length, "")
    }

    /**
     * Convert the specified splitter
     */
    @JvmStatic
    fun getHexString(b: ByteArray, length: Int, split: String): String {
        val hex = StringBuilder("")
        var temp: String
        for (i in 0 until length) {
            temp = Integer.toHexString(b[i].toInt() and 0xFF)
            if (temp.length == 1) {
                temp = "0$temp"
            }
            hex.append(temp + split)
        }
        return hex.toString().trim { it <= ' ' }.uppercase(Locale.getDefault())
    }

    /**
     * String to hexadecimal array
     */
    @JvmStatic
    fun getHexByteArray(hexString: String?): ByteArray? {
        var hexString = hexString
        val buffer = ByteArray(hexString!!.length / 2)
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        for (i in 0 until length) {
            val pos = i * 2
            buffer[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(
                hexChars[pos + 1]
            ).toInt()).toByte()
        }
        return buffer
    }

    /**
     * String to hexadecimal array (Specified length)
     */
    @JvmStatic
    fun getHexByteArray(hexString: String, buffer: ByteArray, nSize: Int): Int {
        var hexString = hexString
        var nSize = nSize
        hexString.replace(" ", "")
        if (nSize > hexString.length / 2) {
            nSize = hexString.length / 2
            if (hexString.length == 1) {
                nSize = 1
                val str = "0"
                hexString = str + hexString
            }
        }
        val hexChars = hexString.toCharArray()
        for (i in 0 until nSize) {
            val pos = i * 2
            buffer[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(
                hexChars[pos + 1]
            ).toInt()).toByte()
        }
        return nSize
    }

    /**
     *
     */
    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    /**
     * byte array to hexadecimal string
     */
    fun ByteArrayToString(bt_ary: ByteArray?): String {
        val sb = StringBuilder()
        if (bt_ary != null) for (b in bt_ary) {
            sb.append(String.format("%02X ", b))
        }
        return sb.toString()
    }
}