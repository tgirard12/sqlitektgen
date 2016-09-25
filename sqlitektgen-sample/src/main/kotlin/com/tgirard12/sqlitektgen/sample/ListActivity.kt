package com.tgirard12.sqlitektgen.sample

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ListActivity : AppCompatActivity() {

    val toolbar by lazy { findViewById(R.id.toolbar) as Toolbar }
    val recyclerView by lazy { findViewById(R.id.recyclerView) as RecyclerView }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()

        // Get All users
        val cursor = App.database.rawQuery(UserDb.SELECT_ALL_ORDER_BY_NAME, null)
        val users = cursor.list {
            UserDb(cursor).copy(group = GroupDb(cursor))
        }

        recyclerView.adapter = UserAdapter(users)
    }


    companion object {
        class UserAdapter(val users: List<UserDb>) : RecyclerView.Adapter<UserViewHolder>() {
            override fun getItemCount(): Int = users.size
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder
                    = UserViewHolder(View.inflate(parent.context, R.layout.view_list_item_2, null))

            override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
                holder.bind(users[position])
            }
        }

        class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val text1: TextView
            val text2: TextView

            init {
                text1 = view.findViewById(R.id.text_1) as TextView
                text2 = view.findViewById(R.id.text_2) as TextView
            }

            fun bind(userDb: UserDb) {
                text1.text = userDb.name
                text2.text = "${userDb.email} - ${userDb.group?.groupNname ?: ""}"
            }
        }
    }
}

