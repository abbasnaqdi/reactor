package com.aaaamirabbas.sample

import com.aaaamirabbas.reactor.handler.Reactor
import com.aaaamirabbas.reactor.helper.Base64Utils

class ReactorUtils(private val reactor: Reactor) {

     fun add() {
        reactor.put("name", "abbas")
        reactor.put<Int>("age", null)
        reactor.put("life", 1L)
        reactor.put("is_man", true)
        reactor.put("array", arrayOf(2, 3, 4, "6", false))
        reactor.put("sampleData", SampleData())
        reactor.putString("sampleData", SampleData().toString())

        println("ADDED")
    }


    fun edit() {
        reactor.put("name", "abbas naqdi")
        reactor.put<Boolean>("is_man", null)

        println("EDITED")
    }

    fun removeData() {
        reactor.remove<String>("name")     //delete multi key
        // reactor.eraseAllData()

        println("REMOVED")
    }

    fun getKeyValue(): String {
        val name = reactor.get<String>("name")
        val age = reactor.get("age", 0)
        val life = reactor.get<Long>("life")
        val man = reactor.get("is_man", false)
        val array = reactor.get<Array<Int>>("array")
        val sampleData = reactor.get<SampleData>("sampleData")
        val sampleDataUnsafe = reactor.getString("sampleData")

        val first: String? = reactor.get("first")
        val second: String = reactor.get("second", "def value")

        return "name -> $name\n" +
                "age -> $age\n" +
                "life -> $life\n" +
                "is_man -> $man\n" +
                "array -> $array\n" +
                "first -> $first\n" +
                "second -> $second\n" +
                "sampleData -> $sampleData\n" +
                "sampleDataUnsafe -> $sampleDataUnsafe\n"
    }
}
