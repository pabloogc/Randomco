package mini.flux

import java.util.concurrent.atomic.AtomicInteger

/**
 * Observes, modifies, and potentially short-circuits actions going through the dispatcher.
 */
typealias Interceptor = (Action, Chain) -> Action

val actionCounter = AtomicInteger()

/**
 * A chain of interceptors. Call [.proceed] with the intercepted action or directly handle it.
 */
interface Chain {
    /**
     * Calls the interceptor chain for a given [Action].
     */
    fun proceed(action: Action): Action
}