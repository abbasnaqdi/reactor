package com.oky2abbas.sample


import com.oky2abbas.reactor.handler.Reactor


class Handler(private val reactor: Reactor) {
    fun add() {
        reactor.put("name", "abbas")
        reactor.put("age", 23)
        reactor.put("life", 1L)
        reactor.put("is_man", true)
        reactor.put("array", arrayOf(2, 3, 4, "6", false))
        reactor.put("tc", this::class.java)
    }

    fun edit() {
        reactor.put("name", "abbas naqdi")
    }

    fun removeAll() {
        reactor.remove("age", 0)     //delete one key
        reactor.clearAll()                      //delete all keys
    }

    fun getKeyValue(): String {
        val name = reactor.get("name", "")
        val age = reactor.get("age", 0)
        val life = reactor.get("life", 0L)
        val man = reactor.get("is_man", false)
        val array = reactor.get("array", arrayOf<Any>())
        val tc = reactor.get("class", this::class.java)

        return "name -> $name\n" +
                "age -> $age\n" +
                "life -> $life\n" +
                "is_man -> $man\n" +
                "array -> $array\n" +
                "class -> $tc\n"
    }
}
