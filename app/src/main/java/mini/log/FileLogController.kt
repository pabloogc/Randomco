package mini.log

import android.content.Context
import android.support.v4.util.Pools
import android.util.Log
import android.util.Log.ERROR
import android.util.Log.getStackTraceString
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

private val LOG_FILES_MAX_AGE = TimeUnit.DAYS.toMillis(3)
private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)

/**
 * Factory for [FileTree].
 * Multiple instances of this controller over the same folder are not safe.
 *
 * @param relativeLogFolderPath Path for the folder containing multiple log files.
 */
class FileLogController(
    private val context: Context,
    private val relativeLogFolderPath: String = "logs",
    private val minLogLevel: Int = Log.VERBOSE) {

    private var _currentFileTree: FileTree? = null

    val currentFileTree: FileTree
        get() = _currentFileTree!!

    @Suppress("OptionalReturnKeyword")
    val logsFolder: File? by lazy {
        var root = context.getExternalFilesDir(null)
        if (root == null) {
            //Fall back to private directory
            root = context.filesDir
        }
        val logRootDirectory = File(root.absolutePath, relativeLogFolderPath)
        if (!logRootDirectory.exists()) {
            if (!logRootDirectory.mkdir()) {
                Timber.e("Unable to create log directory, nothing will be written on disk")
                return@lazy null
            }
        }
        return@lazy logRootDirectory
    }

    /**
     * Create a new fileTree and close the previous one if present.
     *
     * This operation may block, consider executing it in another thread.
     *
     * @return The logger, or null if the file could not be created.
     */
    fun newFileTree(): FileTree? {
        if (logsFolder == null) return null

        val logFileName = String.format("log-${FILE_NAME_DATE_FORMAT.format(Date())}.txt")
        val logFile = File(logsFolder, logFileName)
        Timber.d("New session, logs will be stored in: ${logFile.absolutePath}")
        _currentFileTree?.exit()
        _currentFileTree = FileTree(logFile, minLogLevel)
        return _currentFileTree
    }

    /**
     * Delete any log files created under [logsFolder] older that `maxAge` in ms.
     *
     * This operation may block, consider executing it in another thread.
     *
     * Current log file wont be deleted.
     *
     * @return Deleted files count.
     */
    @Suppress("NestedBlockDepth")
    fun deleteOldLogs(maxAge: Long = LOG_FILES_MAX_AGE, maxCount: Int = Int.MAX_VALUE): Int {
        var deleted = 0
        val now = System.currentTimeMillis()
        logsFolder?.listFiles()?.let { fileList ->
            fileList
                .sortedByDescending { it.lastModified() }
                .filterIndexed { index, file ->
                    index != 0 && (now - file.lastModified() > maxAge || index > maxCount)
                }
                .forEach { file -> if (file.delete()) deleted++ }
        }
        return deleted
    }
}

/**
 * Logger that writes asynchronously to a file.
 * Automatically infers the tag from the calling class.
 */
class FileTree
/**
 * Create a new FileTree instance that will write in a background thread
 * any incoming logs as long as the level is at least `minLevel`.

 * @param file The file this logger will write.
 * @param minLevel The minimum message level that will be written (inclusive).
 */
(val file: File, private val minLevel: Int) : Timber.Tree() {

    private val queue = ArrayBlockingQueue<LogLine>(100)
    private val pool = Pools.SynchronizedPool<LogLine>(20)

    private val backgroundThread: Thread
    private val writer: Writer?

    init {
        var writer: Writer?
        this.backgroundThread = Thread(Runnable { this.loop() })
        try {
            //Not buffered, we want to write on the spot
            writer = FileWriter(file.absolutePath, true)
            this.backgroundThread.start()
        } catch (e: IOException) {
            writer = null
            Timber.e(e, "Failed to create writer, nothing will be done")
        }

        this.writer = writer
    }

    /**
     * Flush the file, this call is required before application dies or the file will be empty.
     */
    fun flush() {
        try {
            writer?.flush()
        } catch (e: IOException) {
            Timber.e(e, "Flush failed")
        }
    }

    /**
     * Close the file and exit. This method does not block.
     */
    fun exit() {
        this.backgroundThread.interrupt()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < minLevel) return
        enqueueLog(priority, tag, message, t)
    }

    private fun enqueueLog(priority: Int, tag: String?, message: String?, t: Throwable?) {
        var logLine: LogLine? = pool.acquire()
        if (logLine == null) {
            logLine = LogLine()
        }

        logLine.tag = tag
        logLine.message = message
        logLine.level = priority
        logLine.throwable = t
        logLine.date.time = System.currentTimeMillis()

        queue.offer(logLine)
    }

    @Suppress("NestedBlockDepth")
    private fun loop() {
        while (true) {
            try {
                val logLine = queue.take()
                writer?.let {
                    val lines = logLine.format()
                    for (line in lines) {
                        writer.write(line)
                    }
                }
                // Force flushing when an error occurs in order to ensure the error trace is
                // written in the logs file before the app is closed
                if (logLine.level == ERROR) {
                    flush()
                }
                logLine.clear()
                pool.release(logLine)
            } catch (e: InterruptedException) {
                break //We are done
            } catch (e: IOException) {
                Timber.e(e, "Failed to write line")
                break
            }

        }
        closeSilently()
    }

    private fun closeSilently() {
        writer?.let {
            try {
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
    }

    override fun toString(): String = "FileTree{file=${file.absolutePath}}"

    private class LogLine {
        companion object {
            private val LOG_FILE_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US)
        }

        internal val date = Date()
        internal var level: Int = 0
        internal var message: String? = null
        internal var tag: String? = null
        internal var throwable: Throwable? = null

        internal fun clear() {
            message = null
            tag = null
            throwable = null
            date.time = 0
            level = 0
        }

        internal fun format(): List<String> {
            val formattedMessage: String
            if (message == null || message!!.trim().isEmpty()) {
                formattedMessage = getStackTrace(throwable)
            } else {
                formattedMessage = message + "\n" + getStackTraceString(throwable)
            }

            formattedMessage.let {
                if (it.isNotEmpty()) {
                    val lines = it.split('\n').dropLastWhile(String::isEmpty)
                    val levelString: String = when (level) {
                        Log.DEBUG -> "D"
                        Log.INFO -> "I"
                        Log.WARN -> "W"
                        Log.ERROR -> "E"
                        else -> "V"
                    }
                    //[29-04-1993 01:02:34.567 D/SomeTag: The value to Log]
                    val prelude = "[${LOG_FILE_DATE_FORMAT.format(date)}] $levelString/$tag"
                    return lines.map { "$prelude $it \r\n" }
                }
                return emptyList()
            }
        }

        internal fun getStackTrace(throwable: Throwable?): String {
            val sw = StringWriter(256)
            val pw = PrintWriter(sw, false)
            throwable?.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
    }
}
