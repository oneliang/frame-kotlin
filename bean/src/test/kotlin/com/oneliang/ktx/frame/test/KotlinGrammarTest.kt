package com.oneliang.ktx.frame.test

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

fun main(args: Array<String>) {
    val byteForField = ByForField()
    byteForField.field = "1"//original value:1, need to set value:1
    println(byteForField.field)
    byteForField.lazyField
    byteForField.lazyField
    byteForField.observerStringField = "observer"
    byteForField.observerIntField = 2
}