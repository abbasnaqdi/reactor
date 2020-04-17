package com.oky2abbas.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oky2abbas.reactor.handler.Reactor
import kotlinx.android.synthetic.main.main_view.*

class MainView : AppCompatActivity() {

    private lateinit var reactor: Reactor
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)

        initObject()
        initView()
    }

    private fun initObject() {
        reactor = Reactor(applicationContext)
        handler = Handler(reactor)
    }

    private fun initView() {
        clickListener()
    }

    private fun clickListener() = btnPrint.setOnClickListener {
        handler.add()
        handler.edit()

        print(handler.getKeyValue())

        handler.removeAll()
    }

    private fun print(text: String) {
        txtPrint.text = text
    }
}
