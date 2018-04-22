package mini.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import dagger.android.support.DaggerAppCompatActivity
import mini.extensions.argument
import kotlin.reflect.KClass

/**
 * [AppCompatActivity] that can hold a [Fragment] to ease testing isolated fragments with Espresso.
 *
 * It assumes that the [Fragment] that we want to load has a default constructor.
 */
@RestrictTo(RestrictTo.Scope.TESTS)
class TestFragmentActivity : DaggerAppCompatActivity() {

    companion object {
        private const val FRAGMENT_CLASS_KEY = "fragment_class"
        private const val FRAGMENT_ARGS_KEY = "fragment_args"
        private val CONTENT_ID = View.generateViewId()

        fun <T : Fragment> createIntent(context: Context, clazz: KClass<T>, args: Bundle? = null): Intent {
            return Intent(context, TestFragmentActivity::class.java).apply {
                putExtra(FRAGMENT_CLASS_KEY, clazz.java.canonicalName)
                putExtra(FRAGMENT_ARGS_KEY, args)
            }
        }
    }

    private lateinit var content: FrameLayout
    private val fragmentClassName: String by argument(FRAGMENT_CLASS_KEY)
    private val fragmentArgs: Bundle? by argument(FRAGMENT_ARGS_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = FrameLayout(this)
        content.id = CONTENT_ID
        content.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

        setContentView(content)

        try {
            val fragment = Fragment.instantiate(this@TestFragmentActivity, fragmentClassName, fragmentArgs)
            supportFragmentManager.beginTransaction()
                .replace(CONTENT_ID, fragment)
                .commit()
        } catch (e: Exception) {
            throw UnsupportedOperationException("The fragment with class name $fragmentClassName could not be instantiated")
        }
    }

    fun findFragment(): Fragment {
        return supportFragmentManager.findFragmentById(CONTENT_ID)!!
    }
}