package mini.extensions

import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable

/**
 * Converts a given [Map] to a [Bundle] ready to use.
 */
@Suppress("SpreadOperator")
fun Map<String, *>.toBundle(): Bundle {
    val values = this.entries.map { it.key to it.value }
    return bundleOf(pairs = *values.toTypedArray())
}

private fun bundleOf(vararg pairs: Pair<String, *>): Bundle {
    return Bundle().apply {
        pairs.forEach {
            val (k, v) = it
            when (v) {
                is IBinder -> putBinder(k, v)
                is Bundle -> putBundle(k, v)
                is Byte -> putByte(k, v)
                is ByteArray -> putByteArray(k, v)
                is Char -> putChar(k, v)
                is CharArray -> putCharArray(k, v)
                is CharSequence -> putCharSequence(k, v)
                is Float -> putFloat(k, v)
                is FloatArray -> putFloatArray(k, v)
                is Parcelable -> putParcelable(k, v)
                is Int -> putInt(k, v)
                is Long -> putLong(k, v)
                is Short -> putShort(k, v)
                is ShortArray -> putShortArray(k, v)
                else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
            }
        }
    }
}