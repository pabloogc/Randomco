package mini.flux

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

/**
 * Dispatch actions and subscribe to them in order to produce changes.
 */
class Dispatcher {
    companion object {
        const val DEFAULT_PRIORITY: Int = 100
    }

    var dispatching: Boolean = false
        private set

    private val subscriptionMap = HashMap<Class<*>, TreeSet<DispatcherSubscription<Any>>?>()
    private var subscriptionCounter = AtomicInteger()

    private val interceptors = ArrayList<Interceptor>()
    private val rootChain: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            action.tags.forEach { tag ->
                subscriptionMap[tag]?.let { set ->
                    set.forEach { it.onAction(action) }
                }
            }
            return action
        }
    }
    private var chain = rootChain
    private fun buildChain(): Chain {
        return interceptors.fold(rootChain)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
        }
    }

    /**
     * Adds an action interceptor to the interceptor chain.
     */
    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            chain = buildChain()
        }
    }

    /**
     * Removes an action interceptor to the interceptor chain.
     */
    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            chain = buildChain()
        }
    }

    /**
     * Dispatches an action to any store.
     */
    fun dispatch(action: Action) {
        assertOnUiThread()
        synchronized(this) {
            try {
                if (dispatching) error("Can't dispatch actions while reducing state!")
                actionCounter.incrementAndGet()
                dispatching = true
                chain.proceed(action)
            } finally {
                dispatching = false
            }
        }
    }

    /**
     * Post an event that will dispatch the action on the Ui thread
     * and return immediately.
     */
    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    /**
     * Post and event that will dispatch the action on the Ui thread
     * and block until the dispatch is complete.
     *
     * Can't be called from the main thread.
     */
    fun dispatchOnUiSync(action: Action) {
        onUiSync { dispatch(action) }
    }

    /**
     * Returns a subscription to listen to given action emissions.
     */
    fun <T : Any> subscribe(tag: KClass<T>, fn: (T) -> Unit = {}) = subscribe(DEFAULT_PRIORITY, tag, fn)

    /**
     * Returns a subscription to listen to given action emissions.
     */
    fun <T : Any> subscribe(priority: Int,
                            tag: KClass<T>,
                            fn: (T) -> Unit = {}): DispatcherSubscription<T> {
        val subscription = DispatcherSubscription(
            this,
            subscriptionCounter.getAndIncrement(),
            priority,
            tag.java,
            fn)
        return registerInternal(subscription)
    }

    /**
     * Resets the dispatcher value for tests purposes.
     */
    @TestOnly
    fun clearDispatcher() {
        subscriptionMap.clear()
        subscriptionCounter.set(0)
    }

    internal fun <T : Any> registerInternal(dispatcherSubscription: DispatcherSubscription<T>): DispatcherSubscription<T> {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            subscriptionMap.getOrPut(dispatcherSubscription.tag, {
                TreeSet { a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                }
            })!!.add(dispatcherSubscription as DispatcherSubscription<Any>)
        }
        return dispatcherSubscription
    }

    internal fun <T : Any> unregisterInternal(dispatcherSubscription: DispatcherSubscription<T>) {
        synchronized(this) {
            val set = subscriptionMap[dispatcherSubscription.tag] as? TreeSet<*>
            val removed = set?.remove(dispatcherSubscription) == true
            if (!removed) {
                Timber.w("Failed to remove dispatcherSubscription, multiple dispose calls?")
            }
        }
    }
}

/**
 * Custom [Disposable] that handles [Dispatcher] subscriptions.
 */
class DispatcherSubscription<T : Any>(internal val dispatcher: Dispatcher,
                                      internal val id: Int,
                                      internal val priority: Int,
                                      internal val tag: Class<T>,
                                      private val cb: (T) -> Unit) : Disposable {
    private var processor: PublishProcessor<T>? = null
    private var subject: PublishSubject<T>? = null
    private var disposed = false

    override fun isDisposed(): Boolean = disposed

    internal fun onAction(action: T) {
        if (disposed) {
            Timber.e("Subscription is disposed but got an action: $action")
            return
        }
        cb.invoke(action)
        processor?.onNext(action)
        subject?.onNext(action)
    }

    /**
     * Returns the subscription as a [Flowable].
     */
    fun flowable(): Flowable<T> {
        if (processor == null) {
            synchronized(this) {
                if (processor == null) processor = PublishProcessor.create()
            }
        }
        return processor!!
    }

    /**
     * Returns the subscription as an [Observable].
     */
    fun observable(): Observable<T> {
        if (subject == null) {
            synchronized(this) {
                if (subject == null) subject = PublishSubject.create()
            }
        }
        return subject!!
    }

    override fun dispose() {
        if (disposed) return
        synchronized(this) {
            dispatcher.unregisterInternal(this)
            disposed = true
            processor?.onComplete()
            subject?.onComplete()
        }
    }
}
