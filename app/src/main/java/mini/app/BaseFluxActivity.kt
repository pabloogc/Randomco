package mini.app

import dagger.android.support.DaggerAppCompatActivity
import mini.flux.DefaultSubscriptionTracker
import mini.flux.Dispatcher
import mini.flux.SubscriptionTracker
import javax.inject.Inject

/** Base [Activity] to use with Flux+Dagger in the app. */
abstract class BaseFluxActivity :
    DaggerAppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject lateinit var dispatcher: Dispatcher

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}