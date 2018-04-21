package mini.flux

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import mini.app.OnActivityLifeCycleAction
import mini.app.OnActivityLifeCycleAction.ActivityStage.*
import mini.app.OnTrimMemoryAction
import timber.log.Timber

/**
 * Handy aliases to use with Dagger
 */
typealias StoreMap = Map<Class<*>, Store<*>>

typealias LazyStoreMap = dagger.Lazy<Map<Class<*>, Store<*>>>

/**
 * Implement this interface from any component that provides stores.
 */
interface StoreHolderComponent {
    /**
     * Returns the stored stores map.
     */
    fun stores(): StoreMap
}

/**
 * Utility methods to work with stores.
 */
object FluxUtil {
    /**
     * Sort and create Stores initial state.
     */
    fun initStores(uninitializedStores: Iterable<Store<*>>) {
        val now = System.currentTimeMillis()

        val stores = uninitializedStores.sortedBy { it.properties.initOrder }

        val initTimes = LongArray(stores.size)
        for (i in 0 until stores.size) {
            val start = System.currentTimeMillis()
            stores[i].init()
            stores[i].state //Create initial state
            initTimes[i] += System.currentTimeMillis() - start
        }

        val elapsed = System.currentTimeMillis() - now

        Timber.d("┌ Application with ${stores.size} stores loaded in $elapsed ms")
        Timber.d("├────────────────────────────────────────────")
        for (i in 0 until stores.size) {
            val store = stores[i]
            var boxChar = "├"
            if (store === stores[stores.size - 1]) {
                boxChar = "└"
            }
            Timber.d("$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms")
        }
    }

    /**
     * Register callbacks to send [OnTrimMemoryAction] and [OnActivityLifeCycleAction].
     */
    fun registerSystemCallbacks(dispatcher: Dispatcher, context: Context) {
        val app = context.applicationContext as? Application

        app?.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onLowMemory() {}

            override fun onConfigurationChanged(newConfig: Configuration?) {}

            override fun onTrimMemory(level: Int) {
                dispatcher.dispatch(OnTrimMemoryAction(level))
            }
        })

        app?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, CREATED))

            override fun onActivityStarted(activity: Activity) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, STARTED))

            override fun onActivityResumed(activity: Activity) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, RESUMED))

            override fun onActivityPaused(activity: Activity) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, PAUSED))

            override fun onActivityStopped(activity: Activity) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, STOPPED))

            override fun onActivityDestroyed(activity: Activity) =
                dispatcher.dispatch(OnActivityLifeCycleAction(activity, DESTROYED))

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
        })
    }
}
