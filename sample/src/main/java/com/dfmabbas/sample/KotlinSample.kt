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

        //init reactor db
        reactor = Reactor(this.context!!, "settings", SecurityLevel.POWERFUL)
    }

    private fun sampleCode() {

        // insert or update value data by key
        reactor?.put("name", "abbas")
        reactor?.put("age", 23)
        reactor?.put("is_man", true)

        //get value by key
        val name = reactor?.get("name", "")
        val age = reactor?.get("age", 1)
        val man = reactor?.get("is_man", false)

        Log.i("name ->", name);
        Log.i("age ->", age.toString());
        Log.i("is_man ->", man.toString());
    }
}
