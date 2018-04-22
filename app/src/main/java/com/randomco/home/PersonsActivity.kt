package com.randomco.home

import android.os.Bundle
import android.support.v7.widget.SearchView
import com.randomco.app.LoadPersonsAction
import com.randomco.app.PersonsStore
import com.randomco.app.UpdateFilterAction
import com.randomco.models.LatLon
import com.randomco.models.PersonsFilter
import com.randomco.randomco.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.persons_activity.*
import kotlinx.android.synthetic.main.persons_filters.*
import mini.app.BaseFluxActivity
import mini.extensions.select
import mini.extensions.showToast
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val PERSONS_TO_LOAD = 40

class MainActivity : BaseFluxActivity() {

    @Inject lateinit var personsStore: PersonsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.persons_activity)

        if (personsStore.state.persons == null) {
            dispatcher.dispatch(LoadPersonsAction(PERSONS_TO_LOAD))
        }

        inflateMenu()
        configureFilters()
        configureLoadingFeedback()
        attachFragments(savedInstanceState)

    }

    private fun inflateMenu() {
        //Load more menu button
        toolbar.inflateMenu(R.menu.persons_menu)
        toolbar.menu.findItem(R.id.load_more_action)
            .setOnMenuItemClickListener {
                dispatcher.dispatch(LoadPersonsAction(PERSONS_TO_LOAD))
                true
            }
    }

    private fun attachFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val isTablet = resources.getBoolean(R.bool.is_tablet)
            if (isTablet) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, PersonsFragment.newInstance())
                    .add(R.id.second_fragment_container, PersonsFragment.newInstance(onlyFav = true))
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, PersonsFragment.newInstance())
                    .commit()
            }
        }
    }

    private fun configureLoadingFeedback() {
        personsStore.flowable()
            .select { it.loadPersonsTask }
            .subscribe {
                if (it.isRunning()) {
                    refresh_layout.isEnabled = true
                    refresh_layout.isRefreshing = true
                } else {
                    refresh_layout.isEnabled = false
                    refresh_layout.isRefreshing = false
                }

                if (it.isFailure()) {
                    showToast(it.error!!.message ?: "Unexpected error")
                }
            }
    }

    private fun configureFilters() {
        //Filter actions
        val searchView = toolbar.menu.findItem(R.id.search_action).actionView as SearchView
        fun updateFilter() {
            val filter = PersonsFilter(
                textFilter = if (searchView.query.isNullOrBlank()) null else searchView.query.toString(),
                distanceFilter = if (distance_checkbox.isChecked) 1.0f else null,
                currentLocation = LatLon.MADRID, //TODO: Actual location?
                sortByGender = gender_sort_checkbox.isChecked,
                sortByName = name_sort_checkbox.isChecked
            )
            dispatcher.dispatch(UpdateFilterAction(filter))
        }
        gender_sort_checkbox.setOnClickListener { updateFilter() }
        name_sort_checkbox.setOnClickListener { updateFilter() }
        distance_checkbox.setOnClickListener { updateFilter() }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private val processor = PublishProcessor.create<String>()

            init {
                //Wait for 300ms without typing before updating the filters
                processor
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        updateFilter()
                    }.track()
            }

            override fun onQueryTextSubmit(text: String?): Boolean {
                updateFilter()
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                processor.onNext(text)
                return true
            }
        })
        //TODO: Make filter view reactive
    }
}


