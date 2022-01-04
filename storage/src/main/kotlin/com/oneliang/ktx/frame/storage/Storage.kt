package com.oneliang.ktx.frame.storage

interface Storage {

    /**
     * add value to a key
     * @param key
     * @param value
     */
    fun add(key: String, value: String)

    /**
     * because one key includes multi value
     * @param key
     * @param value
     */
    fun delete(key: String, value: String)

    /**
     * because one key includes multi value
     * @param key
     */
    fun search(key: String): List<String>

    /**
     * update a key with new value, but need to find
     * @param key
     * @param originalValue
     * @param newValue
     */
    fun update(key: String, originalValue: String, newValue: String)

    /**
     * hit the key with actual value, always use it after search
     * @param key
     * @param value
     */
    fun hit(key: String, value: String)
}