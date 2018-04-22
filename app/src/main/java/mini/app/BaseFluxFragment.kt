package mini.app

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import dagger.android.support.DaggerFragment
import mini.flux.DefaultSubscriptionTracker
import mini.flux.Dispatcher
import mini.flux.SubscriptionTracker
import javax.inject.Inject

/** Base [Fragment] to use with Flux+Dagger in the app. */
@SuppressLint("Registered")
abstract class BaseFluxFragment :
    DaggerFragment(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject lateinit var dispatcher: Dispatcher

    override fun onDestroyView() {
        super.onDestroyView()
        cancelSubscriptions()
    }
}