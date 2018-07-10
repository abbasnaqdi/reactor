package com.dfmabbas.sample


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfmabbas.reactor.handler.Reactor
import com.dfmabbas.reactor.handler.SecurityLevel
import kotlinx.android.synthetic.main.fragment_kotlin_sample.*

class KotlinSample : Fragment() {

    private var reactor: Reactor? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_kotlin_sample, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_kotlin_click.setOnClickListener { sampleCode() }

        reactor = Reactor(context!!)                                //application context
                .setDatabaseName("simple_db")                       //optional database name
                .setSecurityLevel(SecurityLevel.POWERFUL)           //optional cryptographic algorithm
                .build()                                            //build class
    }

    private fun sampleCode() {

        //simple

        reactor?.remove("age", 0)                         //remove key age -> 0 is a type number
        reactor?.clearAll()                                          //clear all key-value

        reactor?.put("name", "abbas")                                //put name to string json
        reactor?.put("age", 23)                                      //put age to int json
        reactor?.put("is_man", true)                                 //put is_man to boolean json

        //or ...

        val name = reactor?.get("name", "")       //get name of string json
        val age = reactor?.get("age", 1)            //get age of int json
        val man = reactor?.get("is_man", false) //get is_man of boolean json

        //or ...

        Log.i("name -> ", name)
        Log.i("age -> ", age.toString())
        Log.i("is_man -> ", name)
    }
}
