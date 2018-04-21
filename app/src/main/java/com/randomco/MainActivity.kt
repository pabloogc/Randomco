package com.randomco

import android.os.Bundle
import com.randomco.app.LoadPersonsAction
import com.randomco.app.PersonStore
import com.randomco.randomco.R
import kotlinx.android.synthetic.main.activity_main.*
import mini.app.BaseFluxActivity
import mini.extensions.select
import javax.inject.Inject

const val PERSONS_TO_LOAD = 40

class MainActivity : BaseFluxActivity() {

    @Inject lateinit var personsStore: PersonStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (personsStore.state.persons == null) {
            dispatcher.dispatch(LoadPersonsAction(PERSONS_TO_LOAD))
        }

        personsStore.flowable()
            .select { it.persons }
            .subscribe {
                text.text = "${it.count()}"
            }

        text.setOnClickListener {
            dispatcher.dispatch(LoadPersonsAction(PERSONS_TO_LOAD))
        }
    }
}
