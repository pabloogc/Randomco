package mini.extensions

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.widget.Toast

/**
 * Retrieve an argument from the arguments map in a [Fragment].
 */
@Suppress("UNCHECKED_CAST")
fun <T> Fragment.argument(key: String): Lazy<T> = lazy { this.arguments?.get(key) as T }

/**
 * Displays a text as a toast in the current fragment.
 *
 * @param text Text to display in the toast
 * @param duration Duration, one of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
 */
fun Fragment.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

/**
 * Displays a text as a toast in the current fragment.
 *
 * @param stringResId Text to display in the toast as a string resource ID
 * @param duration Duration, one of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
 */
fun Fragment.showToast(@StringRes stringResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, stringResId, duration).show()
}