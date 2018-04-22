package mini

import com.randomco.app.app
import mini.flux.Action
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * This [TestRule] evaluates every action received with the [TestDispatcherInterceptor] to
 * intercept all the actions dispatched during a test and block them, getting them not reaching the store.
 */
class TestInterceptorRule : TestRule {
    private val testInterceptor = TestDispatcherInterceptor()
    val actions: List<Action> get() = testInterceptor.actions

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val dispatcher = app.component.dispatcher()
                dispatcher.addInterceptor(testInterceptor)
                base.evaluate() //Execute the test
                dispatcher.removeInterceptor(testInterceptor)
            }
        }
    }

}

/**
 * Returns a new [TestInterceptorRule].
 */
fun testInterceptorRule() = TestInterceptorRule()