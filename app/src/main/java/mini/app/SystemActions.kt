package mini.app

import android.app.Activity
import mini.flux.Action
import mini.log.SilentTag

/**
 * Action triggered when the app needs to trim memory.
 */
data class OnTrimMemoryAction(val level: Int) : Action

/**
 * Action triggered when the activity lifecycle stage changes.
 */
data class OnActivityLifeCycleAction(val activity: Activity,
                                     val stage: OnActivityLifeCycleAction.ActivityStage)
    : Action, SilentTag {
    /**
     * Enum that list possible activity stages in the activity lifecycle.
     */
    enum class ActivityStage {
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED,
        RESTARTED,
        DESTROYED,
    }
}