package mini

import mini.flux.Action
import mini.flux.Chain
import mini.flux.Interceptor
import timber.log.Timber
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
class TestDispatcherInterceptor : Interceptor {

    private val mutedActions = LinkedList<Action>()
    /** Replace all actions with dummy ones */
    override fun invoke(action: Action, chain: Chain): Action {
        Timber.d("Muted: $action")
        mutedActions.add(action)
        return TestOnlyAction
    }

    val actions: List<Action> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
object TestOnlyAction : Action