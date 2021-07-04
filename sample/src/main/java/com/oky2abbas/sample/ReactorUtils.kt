package com.oky2abbas.sample


import com.oky2abbas.reactor.handler.Reactor


class ReactorUtils(private val reactor: Reactor) {
    fun add() {
        reactor.put("name", "abbas")
        reactor.put("age", null)
        reactor.put("life", 1L)
        reactor.put("is_man", true)
        reactor.put("array", arrayOf(2, 3, 4, "6", false))
        reactor.put("sampleData", SampleData())
    }


    fun edit() {
        reactor.put("name", "abbas naqdi")
    }

    fun removeData() {
        reactor.remove<Int>("age", "age2")     //delete multi key
        reactor.eraseAllData()                      //delete all keys
    }

    fun getKeyValue(): String {
        val name = reactor.get<String>("name")
        val age = reactor.get("age", 0)
        val life = reactor.get("life", 0L)
        val man = reactor.get("is_man", false)
        val array = reactor.get<Array<Int>>("array")
        val sampleData = reactor.get<SampleData>("sampleData")

        val first: String? = reactor.get("first")
        val second: String = reactor.get("second", "def value")

        return "name -> $name\n" +
                "age -> $age\n" +
                "life -> $life\n" +
                "is_man -> $man\n" +
                "array -> $array\n" +
                "first -> $first\n" +
                "second -> $second\n" +
                "sampleData -> $sampleData\n"
    }
}
