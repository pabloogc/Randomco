package com.randomco.home

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import com.randomco.anyPerson
import com.randomco.app.DeletePersonAction
import com.randomco.app.PersonState
import com.randomco.app.PersonsStore
import com.randomco.randomco.R
import kotlinx.android.synthetic.main.persons_fragment.view.*
import mini.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PersonsFragmentTest {

    @get:Rule val activity = testFragment { PersonsFragment.newInstance() }
    private val testInterceptor = testInterceptorRule()

    @get:Rule
    val ruleChain = RuleChain
        .outerRule(cleanStateRule())
        .around(testInterceptor)

    @Test
    fun recycler_display_the_correct_amount_of_items() {
        val personsStore = store<PersonsStore>()
        personsStore.setTestState(
            PersonState(listOf(
                anyPerson,
                anyPerson.copy(email = "b"),
                anyPerson.copy(email = "c")))
        )

        val adapter = activity.fragment.view!!.recycler.adapter
        val has3Items = adapter.itemCount == 3
        Assert.assertTrue("Recycler has 3 items", has3Items)
    }

    @Test
    fun clicking_delete_dispatches_the_correct_action() {
        val personsStore = store<PersonsStore>()
        personsStore.setTestState(
            PersonState(listOf(
                anyPerson,
                anyPerson.copy(email = "b"),
                anyPerson.copy(email = "c")))
        )

        onView(first(withId(R.id.person_delete_button))).perform(click())
            .check { _, _ ->
                val expectedAction = DeletePersonAction(personsStore.state.persons!![0])
                val actionWasDispatched = testInterceptor.actions.contains(expectedAction)
                Assert.assertTrue("Delete was dispatched", actionWasDispatched)
            }
    }

    //TODO: Add more tests for other view actions
}