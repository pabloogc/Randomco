package mini.log

import java.io.File
import java.util.*

/**
 * Represents the app logger state attending to the available log files.
 */
data class LoggerState(val logFiles: Array<File> = emptyArray()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoggerState

        if (!Arrays.equals(logFiles, other.logFiles)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(logFiles)
    }
}