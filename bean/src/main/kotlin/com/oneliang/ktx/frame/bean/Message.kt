package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.json.JsonArray
import com.oneliang.ktx.util.json.JsonObject
import com.oneliang.ktx.util.json.JsonUtil
import kotlin.reflect.KClass

class Message<O : Any, I : Any> {
    companion object {
        private const val FIELD_SUCCESS = "success"
        private const val FIELD_MESSAGE = "message"
        private const val FIELD_OBJECT = "object"
        private const val FIELD_OBJECT_LIST = "objectList"
        private const val FIELD_OTHER_INFORMATION = "otherInformation"

        /**
         * for special class use
         * @param <O>
         * @param <I>
         * @param json
         * @param objectClass
         * @param listItemClass
         * @param classProcessor
         * @return Message
        </I></O> */
        fun <O : Any, I : Any> jsonToMessage(json: String, objectClass: KClass<O>, listItemClass: KClass<I>, classProcessor: KotlinClassUtil.KotlinClassProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR): Message<O, I> {
            val message = Message<O, I>()
            if (json.isNotBlank()) {
                val jsonObject = JsonObject(json)
                message.isSuccess = java.lang.Boolean.parseBoolean(KotlinClassUtil.changeType(Boolean::class, arrayOf(jsonObject.get(FIELD_SUCCESS).toString())).toString())
                message.message = KotlinClassUtil.changeType(String::class, arrayOf(jsonObject.get(FIELD_MESSAGE).toString())).toString()
                var instance = jsonObject.get(FIELD_OBJECT)
                if (instance is JsonObject) {
                    val jsonObjectValue = instance
                    val objectValue = JsonUtil.jsonObjectToObject(jsonObjectValue, objectClass, classProcessor)
                    message.instance = objectValue
                }
                instance = jsonObject.get(FIELD_OBJECT_LIST)
                if (instance is JsonArray) {
                    val jsonArrayValue = instance
                    message.objectList = JsonUtil.jsonArrayToList(jsonArrayValue, listItemClass, classProcessor)
                }
                message.otherInformation = KotlinClassUtil.changeType(String::class, arrayOf(jsonObject.get(FIELD_OTHER_INFORMATION).toString())).toString()
            }
            return message
        }

        /**
         * obtain success message
         * @param <O>
         * @param <I>
         * @param message
         * @param object
         * @param objectList
         * @param otherInformation
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainSuccessMessage(message: String, instance: O? = null, objectList: List<I> = emptyList(), otherInformation: String = Constants.String.BLANK): Message<O, I> {
            return Message(true, message, instance, objectList, otherInformation)
        }

        /**
         * obtain failure message
         * @param <O>
         * @param <I>
         * @param message
         * @param object
         * @param objectList
         * @param otherInformation
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainFailureMessage(message: String, instance: O? = null, objectList: List<I> = emptyList(), otherInformation: String = Constants.String.BLANK): Message<O, I> {
            return Message(false, message, instance, objectList, otherInformation)
        }
    }

    /**
     * @return the success
     */
    /**
     * @param success the success to set
     */
    var isSuccess = false
    /**
     * @return the message
     */
    /**
     * @param message the message to set
     */
    var message = Constants.String.BLANK
    /**
     * @return the object
     */
    /**
     * @param object the object to set
     */
    var instance: O? = null
    /**
     * @return the objectList
     */
    /**
     * @param objectList the objectList to set
     */
    var objectList = emptyList<I>()
    /**
     * @return the otherInformation
     */
    /**
     * @param otherInformation the otherInformation to set
     */
    var otherInformation = Constants.String.BLANK

    constructor()

    /**
     * constructor
     * @param success
     * @param message
     * @param object
     * @param objectList
     */
    constructor(success: Boolean, message: String, instance: O?, objectList: List<I>, otherInformation: String) {
        this.isSuccess = success
        this.message = message
        this.instance = instance
        this.objectList = objectList
        this.otherInformation = otherInformation
    }

    /**
     * to json
     * @param jsonProcessor
     * @return String
     */
    fun toJson(jsonProcessor: JsonUtil.JsonProcessor = JsonUtil.DEFAULT_JSON_PROCESSOR): String {
        return JsonUtil.objectToJson(this, emptyArray(), jsonProcessor)
    }
}
