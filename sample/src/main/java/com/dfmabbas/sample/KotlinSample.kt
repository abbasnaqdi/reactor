package com.dfmabbas.sample


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfmabbas.reactor.handler.Reactor
import com.dfmabbas.reactor.handler.SecurityLevel

class KotlinSample : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_kotlin_sample, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init reactor db
        val reactor = Reactor(this.context!!, "database", SecurityLevel.NONE)

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
