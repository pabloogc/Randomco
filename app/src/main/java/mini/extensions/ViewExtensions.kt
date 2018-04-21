package mini.extensions

import android.view.View

/**
 * Makes the given [View] visible.
 */
fun View.makeVisible() {
    visibility = View.VISIBLE
}

/**
 * Makes the given [View] invisible.
 */
fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

/**
 * Makes the given [View] gone.
 */
fun View.makeGone() {
    visibility = View.GONE
}

/**
 * Returns if the [View] is visible.
 */
val View.isVisible
    inline get() = visibility == View.VISIBLE

/**
 * Returns if the [View] is invisible.
 */
val View.isInvisible
    inline get() = visibility == View.INVISIBLE

/**
 * Returns if the [View] is gone.
 */
val View.isGone
    inline get() = visibility == View.GONE