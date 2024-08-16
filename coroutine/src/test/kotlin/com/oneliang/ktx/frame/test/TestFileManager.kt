package com.oneliang.ktx.frame.test

import com.oneliang.ktx.util.json.jsonToJsonObject
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.toJson

fun main() {
//    val fileManager = FileManager()
    val json = "{\"WECHAT_PAY_PRICE\":\"12800\",\"QUOTE_FREQUENCY\":\"5\",\"WECHAT_PAY_SUCCESS_CODE\":\"PAY_SUCCESS\",\"WORK_PAY_SUCCESS_CODE\":\"PAY_SUCCESS\",\"INFORMATION_FLAG\":\"0\",\"ADVANCE_TIP\":\"1\",\"MERGE_SPOT_COMPLEX_PRODUCT_TYPE_PROPERTY_CONST\":\"[\\\"16118903098660041007\\\"]\",\"DELIVERY_NUMBER_DAYS\":\"60\",\"KEY_OPEN_TEAM_BUYING_ACTIVITY\":\"false\",\"SHORT_DELIVERY_QUOTE\":\"false\",\"FUTURE_ANY_ORIGINAL_PLACE_LOWEST_PRICE\":\"14000\"}"
    json.jsonToJsonObject()

    val string = "[\"16118903098660041007\"]"
    val map = mapOf("MERGE_SPOT_COMPLEX_PRODUCT_TYPE_PROPERTY_CONST" to string)
    println(map.toJson())
    println(map.toJson().jsonToMap())
    val map2= mapOf("record" to map.toJson())
    println(map2.toJson())
    val map22 = map2.toJson().jsonToMap()
    println(map22)
}