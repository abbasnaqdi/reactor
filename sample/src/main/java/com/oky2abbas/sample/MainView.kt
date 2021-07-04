package com.oky2abbas.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oky2abbas.reactor.handler.Reactor
import kotlinx.android.synthetic.main.main_view.*

class MainView : AppCompatActivity() {

    private lateinit var reactor: Reactor
    private lateinit var reactorUtils: ReactorUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)

        initObject()
        initView()
    }

    private fun initObject() {
        reactor = Reactor(applicationContext)
        reactorUtils = ReactorUtils(reactor)
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() = btnPrint.setOnClickListener {
        reactorUtils.add()
        reactorUtils.edit()

        print(reactorUtils.getKeyValue())

//        handler.removeData()
//        print(handler.getKeyValue())
    }

    private fun print(text: String) {
        txtPrint.text = text
    }
}
