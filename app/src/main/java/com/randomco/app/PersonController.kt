package com.randomco.app

import com.randomco.models.PersonMapper
import com.randomco.network.RandomPersonService
import io.reactivex.schedulers.Schedulers
import mini.flux.Dispatcher
import mini.misc.taskFailure
import mini.misc.taskSuccess
import javax.inject.Inject

interface PersonController {
    /**
     * Load [count] persons and dispatch [PersonsLoadedAction] after completed.
     */
    fun loadPersons(count: Int)
}

class PersonControllerImpl @Inject constructor(val dispatcher: Dispatcher,
                                               val randomPersonService: RandomPersonService) : PersonController {
    override fun loadPersons(count: Int) {
        //You can move this to a controller
        randomPersonService.fetchPersons(count)
            .subscribeOn(Schedulers.io())
            .map {
                val personMapper = PersonMapper()
                it.results
                    .map { networkPerson -> personMapper(networkPerson) }
            }
            .subscribe(
                { persons ->
                    dispatcher.dispatchOnUi(PersonsLoadedAction(
                        persons = persons,
                        loadTask = taskSuccess()))
                },
                { error ->
                    dispatcher.dispatchOnUi(PersonsLoadedAction(
                        persons = null,
                        loadTask = taskFailure(error)))
                }
            )
    }
}