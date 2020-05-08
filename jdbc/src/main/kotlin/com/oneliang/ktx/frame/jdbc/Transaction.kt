package com.oneliang.ktx.frame.jdbc

interface Transaction {

    /**
     * transaction execute
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun execute(): Boolean
}
