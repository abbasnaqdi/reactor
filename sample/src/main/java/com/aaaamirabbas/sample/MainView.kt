package com.aaaamirabbas.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aaaamirabbas.reactor.handler.Reactor
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
        reactor = Reactor(applicationContext, false)
        reactorUtils = ReactorUtils(reactor)
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() = btnPrint.setOnClickListener {
        reactorUtils.add()
        reactorUtils.edit()

        log(reactorUtils.getKeyValue())

        reactorUtils.removeData()
        log(reactorUtils.getKeyValue())
    }

    private fun log(text: String) {
        txtPrint.text = text
    }
}
