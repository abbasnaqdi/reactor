package com.dfmabbas.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dfmabbas.reactor.handler.Reactor
import com.dfmabbas.reactor.handler.SecurityLevel

class MainView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)

        //init reactor db
        val reactor = Reactor(applicationContext, "database", SecurityLevel.NONE)

        //insert or update value data by key
        reactor.put("name", "abbas")
        reactor.put("age", 23)
        reactor.put("is_man", true)

        //get value by key
        val name = reactor.getString("name", "ghazal")
        val age = reactor.getInt("age", 1)
        val man = reactor.getBoolean("is_man", false)

        Log.e("name ->", name);
        Log.e("age ->", age.toString());
        Log.e("is_man ->", man.toString());
    }
}
