package com.randomco

import android.os.Bundle
import com.randomco.network.RandomService
import com.randomco.randomco.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import mini.app.BaseFluxActivity
import mini.extensions.showToast
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseFluxActivity() {

    @Inject lateinit var randomService: RandomService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        randomService.fetchPersons(40)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { persons ->
                    showToast("${persons.results}")
                    Timber.d("${persons.results}")
                },
                { error ->
                    error.printStackTrace()
                    showToast(error.message.toString())
                }
            ).track()
    }
}
