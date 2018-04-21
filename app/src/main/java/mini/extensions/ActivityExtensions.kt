package mini.extensions

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

/**
 * Displays a text as a toast in the current activity.
 *
 * @param text Text to display in the toast
 * @param duration Duration, one of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
 */
fun Activity.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

/**
 * Displays a text as a toast in the current activity.
 *
 * @param stringResId Text to display in the toast as a string resource ID
 * @param duration Duration, one of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
 */
fun Activity.showToast(@StringRes stringResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, stringResId, duration).show()
}

/**
 * Hides the keyboard from the activity.
 */
fun Activity.hideKeyboard(focusView: View? = this.currentFocus, force: Boolean = true) {
    if (focusView == null) return
    val flags = if (force) 0 else InputMethodManager.HIDE_IMPLICIT_ONLY
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(focusView.windowToken, flags)
}