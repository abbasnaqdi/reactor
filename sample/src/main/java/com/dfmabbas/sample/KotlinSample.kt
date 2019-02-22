package com.dfmabbas.sample


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dfmabbas.reactor.handler.Reactor
import kotlinx.android.synthetic.main.fragment_kotlin_sample.*


class KotlinSample : Fragment() {

    private val reactor = Reactor(context!!)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_kotlin_sample,
            container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_kotlin_click.setOnClickListener { sampleCode() }
    }

    private fun sampleCode() {

        reactor.put("name", "abbas")
        reactor.put("age", 23)
        reactor.put("is_man", true)
        reactor.put("array", arrayOf(2, 3, 4, "6", false))
        reactor.put("tc", this::class.java)

        val name = reactor.get("name", "")
        val age = reactor.get("age", 0)
        val man = reactor.get("is_man", false)
        val array = reactor.get("array", arrayOf<Any>())
        val tc = reactor.get("class", this::class.java)

        Log.i("name -> ", name)
        Log.i("age -> ", age.toString())
        Log.i("is_man -> ", man.toString())
        Log.i("array -> ", array.size.toString())
        Log.i("tc -> ", tc.name)

        reactor.remove("age", 0)
        reactor.clearAll()
    }
}
