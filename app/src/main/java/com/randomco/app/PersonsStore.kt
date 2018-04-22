package com.randomco.app

import com.randomco.models.Person
import com.randomco.models.PersonsFilter
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.dagger.AppScope
import mini.flux.Action
import mini.flux.Store
import mini.misc.Task
import mini.misc.taskIdle
import mini.misc.taskRunning
import javax.inject.Inject

data class PersonState(
    val persons: List<Person>? = null,
    val loadPersonsTask: Task = taskIdle(),
    val activeFilter: PersonsFilter = PersonsFilter()
)


data class LoadPersonsAction(val count: Int) : Action
data class PersonsLoadedAction(val persons: List<Person>?, val loadTask: Task) : Action
data class DeletePersonAction(val person: Person) : Action
data class TogglePersonFavAction(val person: Person) : Action
data class UpdateFilterAction(val filter: PersonsFilter) : Action

@AppScope
class PersonsStore @Inject constructor(
    private val personController: PersonController) : Store<PersonState>() {

    override fun init() {
        dispatcher.subscribe(LoadPersonsAction::class) {
            if (state.loadPersonsTask.isRunning()) return@subscribe
            state = state.copy(loadPersonsTask = taskRunning())
            personController.loadPersons(count = it.count)
        }

        dispatcher.subscribe(PersonsLoadedAction::class) {
            val newPersons = if (it.loadTask.isSuccessful()) {
                val prevList = state.persons ?: emptyList()
                val appended = prevList + it.persons!!
                appended.distinctBy { it.id }
            } else {
                state.persons
            }
            state = state.copy(
                persons = newPersons,
                loadPersonsTask = it.loadTask
            )
        }

        dispatcher.subscribe(DeletePersonAction::class) {
            val toDelete = it.person
            state = state.copy(
                persons = state.persons?.filter { it != toDelete }
            )
        }

        //This could be a generic UpdatePersonAction, matching by id and replacing
        //the element
        dispatcher.subscribe(TogglePersonFavAction::class) {
            val toUpdate = it.person
            state = state.copy(
                persons = state.persons?.map {
                    if (it == toUpdate) it.copy(favorite = !it.favorite)
                    else it
                }
            )
        }

        dispatcher.subscribe(UpdateFilterAction::class) {
            state = state.copy(activeFilter = it.filter)
        }
    }
}


@Module
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
interface PersonsModule {
    @Binds @AppScope @IntoMap @ClassKey(PersonsStore::class)
    fun storeToMap(store: PersonsStore): Store<*>

    @Binds @AppScope
    fun personController(personControllerImpl: PersonControllerImpl): PersonController
}