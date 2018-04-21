package mini.flux

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Interface that provides functions to track RX Subscriptions
 */
interface SubscriptionTracker {
    /**
     * Clear Subscriptions.
     */
    fun cancelSubscriptions()

    /**
     * Start tracking a disposable.
     */
    fun <T : Disposable> T.track(): T
}

/**
 * Default implementation of [SubscriptionTracker]
 */
class DefaultSubscriptionTracker : SubscriptionTracker {
    private val disposables = CompositeDisposable()
    override fun cancelSubscriptions() = disposables.clear()
    override fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}