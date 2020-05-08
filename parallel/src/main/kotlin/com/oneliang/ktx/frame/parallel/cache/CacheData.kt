package com.oneliang.ktx.frame.parallel.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.json.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class CacheData {
    companion object {
        fun fromJson(json: String): CacheData {
            if (json.isBlank()) {
                return CacheData()
            }
            return json.jsonToObject(CacheData::class, object : DefaultJsonKotlinClassProcessor() {
                override fun <T : Any> changeClassProcess(kClass: KClass<T>, values: Array<String>, fieldName: String): Any? {
                    if (fieldName == "sourceDataMap" || fieldName == "sinkDataMap") {
                        val map = values[0].jsonToMap()
                        val dataMap = ConcurrentHashMap<String, Data>()
                        map.forEach { (key, value) ->
                            dataMap[key] = value.jsonToObject(Data::class)
                        }
                        return dataMap
                    }
                    return super.changeClassProcess(kClass, values, fieldName)
                }
            })
        }
    }

    var sourceDataMap: ConcurrentHashMap<String, Data> = ConcurrentHashMap()
    var sinkDataMap: ConcurrentHashMap<String, Data> = ConcurrentHashMap()

    class Data {
        var data = Constants.String.BLANK
    }
}

fun CacheData.getSourceData(sourceKey: String): CacheData.Data? {
    return this.sourceDataMap[sourceKey]
}

fun CacheData.getSinkData(sinkKey: String): CacheData.Data? {
    return this.sinkDataMap[sinkKey]
}

fun CacheData.updateSourceData(key: String, data: CacheData.Data?) {
    if (data != null) {
        this.sourceDataMap[key] = data
    }
}

fun CacheData.updateSinkData(key: String, data: CacheData.Data?) {
    if (data != null) {
        this.sinkDataMap[key] = data
    }
}