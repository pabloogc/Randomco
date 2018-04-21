package mini.app

import android.annotation.SuppressLint
import dagger.android.support.DaggerAppCompatActivity
import mini.flux.DefaultSubscriptionTracker
import mini.flux.SubscriptionTracker

/** Base [Activity] to use with Flux+Dagger in the app. */
@SuppressLint("Registered")
abstract class BaseFluxActivity :
    DaggerAppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}