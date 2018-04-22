package com.randomco

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.randomco.app.DeletePersonAction
import com.randomco.app.LoadPersonsAction
import com.randomco.app.PersonStore
import com.randomco.app.TogglePersonFavAction
import com.randomco.models.Person
import com.randomco.randomco.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.person_list_item.view.*
import mini.app.BaseFluxActivity
import mini.extensions.select
import javax.inject.Inject

const val PERSONS_TO_LOAD = 40

class MainActivity : BaseFluxActivity() {

    @Inject lateinit var personsStore: PersonStore

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
        setContentView(R.layout.activity_main)

        if (personsStore.state.persons == null) {
            dispatcher.dispatch(LoadPersonsAction(PERSONS_TO_LOAD))
        }

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        personsStore.flowable()
            .select { it.persons }
            .subscribe {
                adapter.items = it
                adapter.notifyDataSetChanged()
            }
            .track()
    }
}


class PersonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image: ImageView = itemView.person_image
    val name: TextView = itemView.person_name_text
    val email: TextView = itemView.person_email_text
    val phone: TextView = itemView.person_email_phone
    val favButton: ImageView = itemView.person_fav_button
    val deleteButton: ImageView = itemView.person_delete_button

    init {
        image.clipToOutline = true //Can't set from XML
    }
}

class PersonAdapter(private val listener: PersonActionsListener) : RecyclerView.Adapter<PersonHolder>() {

    var items: List<Person> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.person_list_item, parent, false)
        return PersonHolder(view)
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PersonHolder, position: Int) {
        val person = items[position]
        holder.apply {
            Picasso.get().load(person.picture.thumb).into(image)

            name.text = "${person.name} ${person.surname}"
            email.text = person.email
            phone.text = person.phone

            val favColor = if (person.favorite) Color.YELLOW else Color.GRAY
            favButton.setColorFilter(favColor)

            favButton.setOnClickListener { listener.onFavClick(person) }
            deleteButton.setOnClickListener { listener.onDeleteClick(person) }
        }
    }

    interface PersonActionsListener {
        fun onDeleteClick(person: Person)
        fun onFavClick(person: Person)
    }
}