package mini.flux

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Semaphore

val uiHandler by lazy { Handler(Looper.getMainLooper()) }

/**
 * Check looper is main looper.
 */
fun isOnUiThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

/**
 * Asserts that the caller function is not called in the UI thread.
 */
fun assertNotOnUiThread() {
    if (isOnUiThread()) {
        throw IllegalStateException(
            "This method can not be called from the main application thread")
    }
}

/**
 * Asserts that the caller function is called in the UI thread.
 */
fun assertOnUiThread() {
    if (!isOnUiThread()) {
        throw IllegalStateException(
            "This method can only be called from the main application thread")
    }
}

/**
 * Executes a function in UI thread.
 */
inline fun onUi(crossinline block: () -> Unit) {
    uiHandler.post { block() }
}

/**
 * Executes a blocking function in UI thread,
 * simply runs the function if already on the UI thread.
 */
inline fun onUiSync(crossinline block: () -> Unit) {
    if (isOnUiThread()) {
        block()
    } else {
        val sem = Semaphore(0)
        onUi {
            block()
            sem.release()
        }
        sem.acquireUninterruptibly()
    }
}