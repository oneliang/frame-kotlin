package com.oneliang.ktx.frame.storage

interface Storage<KEY, VALUE> {

    /**
     * add value to a key
     * @param key
     * @param value
     */
    fun add(key: KEY, value: VALUE)

    /**
     * because one key includes multi value
     * @param key
     * @param value
     */
    fun delete(key: KEY, value: VALUE)

    /**
     * because one key includes multi value
     * @param key
     */
    fun search(key: KEY): List<VALUE>

    /**
     * update a key with new value, but need to find
     * @param key
     * @param originalValue
     * @param newValue
     */
    fun update(key: KEY, originalValue: VALUE, newValue: VALUE)

    /**
     * hit the key with actual value, always use it after search
     * @param key
     * @param value
     */
    fun hit(key: KEY, value: VALUE)

    /**
     * exist values
     * @param key
     * @return Boolean
     */
    fun existValues(key: KEY): Boolean
}