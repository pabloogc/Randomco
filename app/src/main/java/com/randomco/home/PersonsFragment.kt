package com.randomco.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.randomco.app.DeletePersonAction
import com.randomco.app.PersonsStore
import com.randomco.app.TogglePersonFavAction
import com.randomco.models.Person
import com.randomco.randomco.R
import kotlinx.android.synthetic.main.persons_fragment.*
import mini.app.BaseFluxFragment
import mini.extensions.select
import javax.inject.Inject

/**
 * Display all, or only the ones marked as favorite.
 */
enum class PersonsFilterMode {
    ALL, ONLY_FAV
}

class PersonsFragment : BaseFluxFragment() {

    companion object {
        private const val ARG_FILTER_MODE = "filterMode"
        fun newInstance(filterMode: PersonsFilterMode = PersonsFilterMode.ALL): PersonsFragment {
            val args = Bundle().apply {
                putInt(ARG_FILTER_MODE, filterMode.ordinal)
            }
            return PersonsFragment().apply { arguments = args }
        }
    }

    @Inject lateinit var personsStore: PersonsStore
    private lateinit var filterMode: PersonsFilterMode

    private val personActionsListener = object : PersonAdapter.PersonActionsListener {
        override fun onDeleteClick(person: Person) {
            dispatcher.dispatch(DeletePersonAction(person))
        }

        override fun onFavClick(person: Person) {
            dispatcher.dispatch(TogglePersonFavAction(person))
        }

    }
    private val adapter = PersonAdapter(personActionsListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterMode = PersonsFilterMode.values()[arguments?.getInt(ARG_FILTER_MODE) ?: 0]
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.persons_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(
            view.context,
            LinearLayoutManager.VERTICAL,
            false)

        personsStore.flowable()
            .filter { it.persons != null }
            .select { it.persons to it.activeFilter }
            .subscribe { (persons, filter) ->
                adapter.items = filter(persons!!).run {
                    when (filterMode) {
                        PersonsFilterMode.ONLY_FAV -> filter { it.favorite }
                        else -> this
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .track()
    }
}