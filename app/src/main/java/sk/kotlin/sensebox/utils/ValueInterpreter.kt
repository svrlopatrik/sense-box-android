package sk.kotlin.sensebox.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
object ValueInterpreter {

    fun byteArrayToInt(byteArray: ByteArray): Int {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer.int
    }

    fun byteArrayToFloat(byteArray: ByteArray): Float {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer.float
    }

}