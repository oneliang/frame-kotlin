package com.oneliang.ktx.frame.test

import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.common.toUtilDate
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

fun <T : Any> fieldObserver(property: KProperty<*>, oldValue: T, newValue: T) {
    println("property name:${property.name}, old value;$oldValue, new value:$newValue")
}

class ByForField {
    var field: String by FieldDelegate()
    val lazyField: String by lazy {
        //only for val
        println("I am in lazy initializer")
        "I am lazy field"
    }
    var observerStringField: String by Delegates.observable("", onChange = ::fieldObserver)
    var observerIntField: Int by Delegates.observable(0, onChange = ::fieldObserver)
}

class FieldDelegate {
    operator fun setValue(byForField: ByForField, property: KProperty<*>, value: String) {
        println("original value:$value, need to set value:$value")
    }

    operator fun getValue(byForField: ByForField, property: KProperty<*>): String {
        println("get value")
        return "123"
    }

}

class ByForMap(map: Map<String, *>) {
    val field: String by map
}

val String.a: String
    get() {
        return "property extensions"
    }

/**
 * equal above
fun String.getA(): String {
return "property extensions"
}
 **/

val b = {}
val c = fun(a: String): String {
    return a
}

val d = fun String.(a: String): String {
    return a + this.length.toString()
}

fun main(args: Array<String>) {
    println(Date().toFormatString("MMM dd, yyyy", Locale.ENGLISH))
    println("Feb 15, 2020".toUtilDate("MMM dd, yyyy", Locale.ENGLISH))
    return
    val byForField = ByForField()
    byForField.field = "1"//original value:1, need to set value:1
    println(byForField.field)
    byForField.lazyField
    byForField.lazyField
    byForField.observerStringField = "observer"
    byForField.observerIntField = 2
    val byForMap = ByForMap(mapOf("field" to "test"))
    println(byForMap.field)
}