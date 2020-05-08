package com.oneliang.ktx.frame.test


class A {
    fun a() {
    }

    fun b() {
        this.a()
    }

}

class B {
    private fun bbb() {

    }

    private fun A.c() {
        this.a()
        this.b()
        this@B.bbb()
    }

    fun a() {
        val a = A()
        a.c()
    }
}

object Util {
    fun c(a: A) {
        a.a()
        a.b()
    }
}

fun main() {
    val a = A()
    a.apply { }
    a.run {  }
    a.let {  }
    a.also {  }
    //a.c()// = Util.c(a)
}