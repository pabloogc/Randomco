package mini

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.Fragment
import android.view.View
import mini.test.TestFragmentActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass

/**
 * Creates an [ActivityTestRule] with a given intent for an specific activity.
 */
fun <T : Activity> testActivity(clazz: KClass<T>,
                                intentFactory: ((context: Context) -> Intent)? = null): ActivityTestRule<T> {
    //Overriding the rule throws an exception
    if (intentFactory == null) return ActivityTestRule<T>(clazz.java)
    return object : ActivityTestRule<T>(clazz.java) {
        override fun getActivityIntent(): Intent = intentFactory(InstrumentationRegistry.getTargetContext())
    }
}

@Suppress("UNCHECKED_CAST")
open class FragmentTestRule<out T : Fragment> : ActivityTestRule<TestFragmentActivity>(TestFragmentActivity::class.java) {
    val fragment: T get() = activity.findFragment() as T
}

/**
 * Creates a [FragmentTestRule] for an specific fragment.
 */
fun <T : Fragment> testFragment(factory: () -> T): FragmentTestRule<T> {
    return object : FragmentTestRule<T>() {
        override fun getActivityIntent(): Intent {
            val fragment = factory()
            return TestFragmentActivity.createIntent(
                InstrumentationRegistry.getTargetContext(),
                fragment.javaClass.kotlin,
                fragment.arguments)
        }
    }
}

fun first(expected: Matcher<View>): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        private var matched = false

        override fun matchesSafely(item: View): Boolean {
            if (matched) return false
            matched = expected.matches(item)
            return matched
        }

        override fun describeTo(description: Description) {
            description.appendText("Matcher.first( " + expected.toString() + " )")
        }
    }
}