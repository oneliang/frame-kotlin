package com.oneliang.ktx.frame.test

@Experimental
annotation class Api

@Api
class TestApi

@Api
fun main(){
    val testApi = TestApi()

}
