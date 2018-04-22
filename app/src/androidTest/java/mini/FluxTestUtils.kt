package mini

import com.randomco.app.app
import mini.flux.Store
import org.junit.rules.TestRule
import org.junit.runners.model.Statement

/**
 * [TestRule] that resets the state of each Store after an evaluation.
 */
fun cleanStateRule(): TestRule {
    return TestRule { base, description ->
        object : Statement() {
            fun reset() {
                app.component.stores()
                    .values
                    .forEach(Store<*>::resetState)
            }

            override fun evaluate() {
                reset()
                base.evaluate() //Execute the test
                reset()
            }
        }
    }
}

/**
 * Returns a [Store] from the main component of the application.
 */
inline fun <reified T : Store<*>> store(): T {
    return app.component.stores()[T::class.java] as T
}