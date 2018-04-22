package com.randomco.app

import android.support.test.runner.AndroidJUnit4
import com.randomco.anyPerson
import junit.framework.Assert
import mini.flux.Dispatcher
import mini.flux.onUiSync
import mini.misc.taskSuccess
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PersonsStoreTest {

    private val dispatcher = Dispatcher()
    private val mockPersonController = object : PersonController {
        override fun loadPersons(count: Int) {

        }
    }

    val store = PersonsStore(dispatcher, mockPersonController)

    init {
        store.init()
    }

    @Before
    fun setUp() {
        store.resetState()
    }

    @Test
    fun duplicated_users_get_removed() {
        onUiSync {
            dispatcher.dispatch(PersonsLoadedAction(listOf(anyPerson, anyPerson), taskSuccess()))
            Assert.assertTrue("Duplicated person gets removed",
                store.state.persons?.size == 1)
        }
    }

    @Test
    fun delete_removes_user() {
        onUiSync {
            store.setTestState(
                PersonState(persons = listOf(anyPerson))
            )
            dispatcher.dispatch(DeletePersonAction(anyPerson))
            Assert.assertTrue("Duplicated person gets removed",
                store.state.persons?.size == 0)
        }
    }

    //TODO: Add more tests for the other reducer functions
}