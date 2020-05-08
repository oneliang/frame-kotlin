package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * CoreQuery interface base on the connection.
 * @author Dandelion
 * @since 2009-08-12
 */
interface BaseQuery {

    /**
     *
     * Method: execute by sql,for all sql
     * @param connection
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBySql(connection: Connection, sql: String, parameters: Array<*> = emptyArray<Any>())

    /**
     *
     * Through the class generate the sql
     *
     * Method: execute query base on connection and  class and selectColumns and table and condition
     * @param <T>
     * @param connection
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return list<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> executeQuery(connection: Connection, kClass: KClass<T>, selectColumns: Array<String> = emptyArray(), table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): List<T>

    /**
     *
     * Method: execute query with id
     * @param <T>
     * @param connection
     * @param kClass
     * @param id
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> executeQueryById(connection: Connection, kClass: KClass<T>, id: IdType): T?

    /**
     *
     * Method: execute query base on the connection and sql command
     * @param connection
     * @param kClass
     * @param sql
     * @param parameters
     * @return List
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun <T : Any> executeQueryBySql(connection: Connection, kClass: KClass<T>, sql: String, parameters: Array<*> = emptyArray<Any>()): List<T>

    /**
     *
     * Method: execute query base on the connection and sql command
     *
     * Caution: use this method must get Statement from the ResultSet and close it and close the ResultSet
     * @param connection
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeQueryBySql(connection: Connection, sql: String, parameters: Array<*> = emptyArray<Any>()): ResultSet

    /**
     *
     * Method: execute insert
     * @param connection
     * @param <T>
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeInsert(connection: Connection, instance: T, table: String = Constants.String.BLANK): Int

    /**
     *
     * Method: execute insert for auto increment
     * @param connection
     * @param <T>
     * @param table
     * @return int for id
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeInsertForAutoIncrement(connection: Connection, instance: T, table: String = Constants.String.BLANK): Int

    /**
     *
     * Method: execute insert collection(list),transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeInsert(connection: Connection, collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: execute update
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun <T : Any> executeUpdate(connection: Connection, instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     *
     * Method: execute update collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeUpdate(connection: Connection, collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: execute delete by id
     * @param <T>
     * @param connection
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> executeDeleteById(connection: Connection, kClass: KClass<T>, id: IdType): Int

    /**
     *
     * Method: execute delete with multi id,transaction
     * @param <T>
     * @param connection
     * @param kClass
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> executeDeleteByIds(connection: Connection, kClass: KClass<T>, ids: Array<IdType>): Int

    /**
     *
     * Method: execute delete
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun <T : Any> executeDelete(connection: Connection, instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     *
     * Method: execute delete
     * @param <T>
     * @param connection
     * @param kClass
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeDelete(connection: Connection, kClass: KClass<T>, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     *
     * Method: execute delete collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> executeDelete(connection: Connection, collection: Collection<T>, table: String = Constants.String.BLANK): IntArray


    /**
     * Method: execute insert for auto increment by sql and return the auto increment id
     * @param connection
     * @param sql
     * @param parameters
     * @return int id
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeInsertForAutoIncrementBySql(connection: Connection, sql: String, parameters: Array<*>): Int

    /**
     *
     * Method: execute update by sql statement
     * @param connection
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeUpdateBySql(connection: Connection, sql: String, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(connection: Connection, sqls: Array<String>): IntArray

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(connection: Connection, sql: String, parametersList: List<Array<*>>): IntArray

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(connection: Connection, batchObjectCollection: Collection<BatchObject>): IntArray

    enum class ExecuteType {
        INSERT, UPDATE_BY_ID, UPDATE_NOT_BY_ID, DELETE_BY_ID, DELETE_NOT_BY_ID
    }

    class BatchObject(val instance: Any, val condition: String = Constants.String.BLANK, val excuteType: ExecuteType)
}
