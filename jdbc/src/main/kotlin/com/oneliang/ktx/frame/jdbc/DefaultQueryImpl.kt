package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.jdbc.mysql.MySqlUtil
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLRecoverableException
import kotlin.reflect.KClass

/**
 * all QueryImpl can extends DefaultQueryImpl,but must initialize the property
 * @author lwx
 * @since 2011-02-12
 */
open class DefaultQueryImpl : BaseQueryImpl(), Query {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultQueryImpl::class)
    }

    private lateinit var connectionPool: ResourcePool<Connection>

    /**
     * use connection
     * @param recoverable only occur SQLRecoverableException can effect
     * @param block
     */
    @Throws(QueryException::class)
    override fun <R> useConnection(recoverable: Boolean, block: (connection: Connection) -> R): R {
        var connection: Connection? = null
        return try {
            connection = this.connectionPool.resource
            block(connection)
        } catch (e: SQLRecoverableException) {
            this.connectionPool.releaseResource(connection, true)
            if (recoverable) {
                logger.error("connection need to recoverable, ready to retry it.", e)
                try {
                    connection = this.connectionPool.resource
                    block(connection)
                } catch (e: Exception) {
                    throw QueryException(e)
                } finally {
                    this.connectionPool.releaseResource(connection)
                }
            } else {
                throw QueryException(e)
            }
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
    }

    /**
     * use stable connection
     * @param recoverable only occur SQLRecoverableException can effect
     * @param block
     */
    @Throws(QueryException::class)
    override fun <R> useStableConnection(recoverable: Boolean, block: (connection: Connection) -> R): R {
        var connection: Connection? = null
        return try {
            connection = this.connectionPool.stableResource
            block(connection)
        } catch (e: SQLRecoverableException) {
            this.connectionPool.releaseStableResource(connection, true)
            if (recoverable) {
                logger.error("connection need to recoverable, ready to retry it.", e)
                try {
                    connection = this.connectionPool.stableResource
                    block(connection)
                } catch (e: Exception) {
                    throw QueryException(e)
                } finally {
                    this.connectionPool.releaseStableResource(connection)
                }
            }
            throw QueryException(e)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseStableResource(connection)
        }
    }

    /**
     * use suitable connection
     * @param useStable
     * @param block
     */
    private fun <R> useSuitableConnection(useStable: Boolean, block: (connection: Connection) -> R): R {
        return if (useStable) {
            useStableConnection(false, block)
        } else {
            useConnection(false, block)
        }
    }

    /**
     * Method: delete object,by table condition just by object id,sql binding
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition = condition, executeType = BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     * Method: delete object not by id,by table condition,sql binding
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObjectNotById(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition = condition, executeType = BaseQuery.ExecuteType.DELETE_NOT_BY_ID)
    }

    /**
     * Method: delete class,by condition
     * @param <T>
     * @param kClass
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(kClass: KClass<T>, condition: String, parameters: Array<*>): Int {
        return useConnection {
            this.executeDelete(it, kClass, condition, parameters)
        }
    }

    /**
     * Method: delete object collection,transaction,not sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     * Method: delete object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> deleteObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     * Method: delete object by id,not sql binding
     * @param <T>
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> deleteObjectById(kClass: KClass<T>, id: IdType): Int {
        return useConnection {
            this.executeDeleteById(it, kClass, id)
        }
    }

    /**
     * Method: delete object by multiple id,transaction,not sql binding
     * @param <T>
     * @param kClass
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> deleteObjectByIds(kClass: KClass<T>, ids: Array<IdType>): Int {
        if (ids.isEmpty()) {
            return 0
        }
        return useConnection {
            this.executeDeleteByIds(it, kClass, ids)
        }
    }

    /**
     * Method: insert object for sql binding
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> insertObject(instance: T, table: String): Int {
        return this.executeUpdate(instance, table, condition = Constants.String.BLANK, executeType = BaseQuery.ExecuteType.INSERT)
    }

    /**
     * Method: insert object for sql binding and return the auto increment id
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    override fun <T : Any> insertObjectForAutoIncrement(instance: T, table: String): Int {
        return this.executeInsertForAutoIncrement(instance, table)
    }

    /**
     * Method: insert object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> insertObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     * Method: insert object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> insertObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     * Method: update object,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param updateFields
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObject(instance: T, table: String, updateFields: Array<String>, condition: String): Int {
        return this.executeUpdate(instance, table, updateFields, condition, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     * Method: update object not by id,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param updateFields
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObjectNotById(instance: T, table: String, updateFields: Array<String>, condition: String): Int {
        return this.executeUpdate(instance, table, updateFields, condition, BaseQuery.ExecuteType.UPDATE_NOT_BY_ID)
    }

    /**
     * Method: update object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     * Method: update object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> updateObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     * Method: select object by id, only for single id column
     * @param <T>
     * @param kClass
     * @param id
     * @param useDistinct
     * @param useStable
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> selectObjectById(kClass: KClass<T>, id: IdType, useDistinct: Boolean, useStable: Boolean): T? {
        return useSuitableConnection(useStable) {
            this.executeQueryById(it, kClass, id, useDistinct)
        }
    }

    /**
     * Method: select object list by ids, only for single id column
     * @param <T>
     * @param kClass
     * @param ids
     * @param useDistinct
     * @param useStable
     * @return List<T>
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> selectObjectListByIds(kClass: KClass<T>, ids: Array<IdType>, useDistinct: Boolean, useStable: Boolean): List<T> {
        return this.useSuitableConnection(useStable) {
            this.executeQueryByIds(it, kClass, ids, useDistinct)
        }
    }

    /**
     * Method: select object, by column, table, condition, parameters,it is sql binding
     * but you must attention it ,it only return the first object, or null when no data
     * @param <T>
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition
     * @param useDistinct
     * @param useStable
     * @param parameters
     * @return T or null
     * @throws QueryException
    </T></T> */
    override fun <T : Any> selectObject(kClass: KClass<T>, selectColumns: Array<String>, table: String, condition: String, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): T? {
        val list = this.selectObjectList(kClass, selectColumns, table, condition, useDistinct, useStable, parameters)
        return if (list.isNotEmpty()) {
            list[0]
        } else {
            null
        }
    }

    /**
     * Method: select object list,by column,table,condition,parameters,it is sql binding
     * @param <T>
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition
     * @param useDistinct
     * @param useStable
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectList(kClass: KClass<T>, selectColumns: Array<String>, table: String, condition: String, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): List<T> {
        return useSuitableConnection(useStable) {
            this.executeQuery(it, kClass, selectColumns, table, condition, useDistinct, parameters)
        }
    }

    /**
     * Method: select object list by sql,it is sql binding
     * @param <T>
     * @param kClass
     * @param sql
     * @param useStable
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectListBySql(kClass: KClass<T>, sql: String, useStable: Boolean, parameters: Array<*>): List<T> {
        return useSuitableConnection(useStable) {
            this.executeQueryBySql(it, kClass, sql, parameters)
        }
    }

    /**
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param kClass
     * @param page
     * @param selectColumns
     * @param table
     * @param condition, maybe conflict with parameter orderBy
     * @param orderBy
     * @param useStable
     * @param useDistinct
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectPaginationList(kClass: KClass<T>, page: Page, countColumn: String, selectColumns: Array<String>, table: String, condition: String, orderBy: String, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): List<T> {
        val totalRows = this.totalRows(kClass, countColumn, table, condition, useDistinct, useStable, parameters)
        val rowsPerPage = page.rowsPerPage
        page.initialize(totalRows, rowsPerPage)
        val startRow = page.pageFirstRow
        val sqlConditions = StringBuilder()
        sqlConditions.append(condition)
        sqlConditions.append(Constants.String.SPACE + orderBy)
        sqlConditions.append(Constants.String.SPACE + Constants.Database.MySql.PAGINATION + Constants.String.SPACE)
        sqlConditions.append(startRow.toString() + Constants.Symbol.COMMA + rowsPerPage)
        return this.selectObjectList(kClass, selectColumns, table, sqlConditions.toString(), useDistinct, useStable, parameters)
    }

    /**
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param <SequenceType>
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition, maybe conflict with parameter orderBy
     * @param sequenceKey
     * @param startSequence
     * @param comparator
     * @param orderBy
     * @param rowPerPage
     * @param useDistinct
     * @param useStable
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    override fun <T : Any, SequenceType : Any> selectObjectFlowList(kClass: KClass<T>, selectColumns: Array<String>, table: String, condition: String, sequenceKey: String, startSequence: SequenceType, comparator: Query.Comparator, orderBy: String, rowPerPage: Int, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): List<T> {
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val fixTable = SqlUtil.fixTable(table, mappingBean, this.sqlProcessor)
            val sql = MySqlUtil.selectPaginationSql(selectColumns, fixTable, condition, sequenceKey, startSequence.toString(), comparator, orderBy, rowPerPage, useDistinct)
            return this.selectObjectListBySql(kClass, sql, useStable, parameters)
        } catch (e: Throwable) {
            throw QueryException(e)
        }
    }

    /**
     * Method: execute by sql ,for all sql,sql binding
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBySql(sql: String, parameters: Array<*>) {
        useConnection {
            this.executeBySql(it, sql, parameters)
        }
    }

    /**
     * Method: execute query by sql statement,use caution,must close the statement
     * @param sql
     * @param useStable
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeQueryBySql(sql: String, useStable: Boolean, parameters: Array<*>): ResultSet {
        return this.useSuitableConnection(useStable) {
            this.executeQueryBySql(it, sql, parameters)
        }
    }

    /**
     * Method: execute query by sql statement for map data
     * @param sql
     * @param columnDataCollection
     * @param useStable
     * @param parameters
     * @return List<Map<String, *>>
     * @throws QueryException
     */
    override fun executeQueryBySqlForMap(sql: String, columnDataCollection: Collection<BaseQuery.ColumnData>, useStable: Boolean, parameters: Array<*>): List<Map<String, *>> {
        return useSuitableConnection(useStable) {
            this.executeQueryBySqlForMap(it, sql, columnDataCollection, parameters)
        }
    }

    /**
     * Method: execute insert for auto increment and return auto increment id
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    protected fun <T : Any> executeInsertForAutoIncrement(instance: T, table: String): Int {
        return useConnection {
            this.executeInsertForAutoIncrement(it, instance, table)
        }
    }

    /**
     * Method: execute update
     * @param instance
     * @param table
     * @param updateFields
     * @param executeType
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(instance: T, table: String, updateFields: Array<String>, condition: String, executeType: BaseQuery.ExecuteType): Int {
        return useConnection {
            this.executeUpdate(it, instance, table, updateFields, condition, executeType)
        }
    }

    /**
     * Method: execute update collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(collection: Collection<T>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        return useConnection {
            this.executeUpdate(it, collection, table, executeType)
        }
    }

    /**
     * Method: execute update collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    protected fun <T : Any, M : Any> executeUpdate(collection: Collection<T>, kClass: KClass<M>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        return useConnection {
            this.executeUpdate(it, collection, kClass, table, executeType)
        }
    }

    /**
     * Method: execute update by sql statement it is sql binding
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeUpdateBySql(sql: String, parameters: Array<*>): Int {
        return useConnection {
            this.executeUpdateBySql(it, sql, parameters)
        }
    }

    /**
     * Method: execute batch,transaction
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(sqls: Array<String>): IntArray {
        return useConnection {
            this.executeBatch(it, sqls)
        }
    }

    /**
     * Method: execute batch
     * @param sqlList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(sqlList: List<String>): IntArray {
        return useConnection {
            this.executeBatch(it, sqlList)
        }
    }

    /**
     * Method: execute batch,transaction
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(sql: String, parametersCollection: Collection<Array<*>>): IntArray {
        return useConnection {
            this.executeBatch(it, sql, parametersCollection)
        }
    }

    /**
     * Method: execute batch,transaction
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray {
        return useConnection {
            this.executeBatch(it, batchObjectCollection)
        }
    }

    /**
     * Method: get the total size, it is sql binding
     * @param <T>
     * @param table
     * @param condition
     * @param useStable
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> totalRows(countColumn: String, table: String, condition: String, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): Int {
        return this.totalRows<T>(null, countColumn, table, condition, useDistinct, useStable, parameters)
    }

    /**
     * Method: get the total size, it is sql binding
     * @param <T>
     * @param kClass
     * @param table
     * @param condition
     * @param useDistinct
     * @param useStable
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> totalRows(kClass: KClass<T>?, countColumn: String, table: String, condition: String, useDistinct: Boolean, useStable: Boolean, parameters: Array<*>): Int {
        val innerCountColumn = if (countColumn.isBlank()) {
            Constants.String.ZERO
        } else {
            if (useDistinct) {
                "DISTINCT $countColumn"
            } else {
                countColumn
            }
        }
        val sql = if (kClass != null) {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass)
            SqlUtil.selectSql(arrayOf("COUNT(${innerCountColumn}) AS " + Constants.Database.COLUMN_NAME_TOTAL), table, condition, useDistinct, mappingBean, this.sqlProcessor)
        } else {
            SqlUtil.selectSql(arrayOf("COUNT(${innerCountColumn}) AS " + Constants.Database.COLUMN_NAME_TOTAL), table, condition, useDistinct, null, this.sqlProcessor)
        }
        val totalList = this.selectObjectListBySql(Total::class, sql, useStable, parameters)
        return if (totalList.isNotEmpty()) {
            totalList[0].total
        } else {
            0
        }
    }

    /**
     * execute transaction, if you need to stop transaction, you can throw exception
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeTransaction(transaction: Transaction): Boolean {
        return executeTransaction(transaction::execute)
    }

    /**
     * execute transaction, if you need to stop transaction, you can throw exception
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeTransaction(transaction: () -> Boolean): Boolean {
        val isFirstIn = !TransactionManager.isCustomTransaction()//first time is not custom transaction, second time is
        return if (isFirstIn) {
            TransactionManager.customTransactionSign.set(true)//must set true before get resource(connection)
            useConnection(recoverable = true) {
                try {
                    it.autoCommit = false
                    val result = transaction()
                    if (!result) {
                        it.rollback()
                    } else {
                        it.commit()
                    }
                    result
                } catch (e: Throwable) {
                    try {
                        it.rollback()
                    } catch (e: Throwable) {
                        throw e
                    }
                    throw e
                } finally {
                    //endTransaction
                    try {
                        it.autoCommit = true
                    } catch (e: Throwable) {
                        throw e
                    } finally {
                        TransactionManager.customTransactionSign.remove()//must set false before release resource(connection)
                    }
                }
            }
        } else {//not first in
            useConnection {
                try {
                    val result = transaction()
                    if (!result) {
                        it.rollback()
                    }
                    result
                } catch (e: Throwable) {
                    try {
                        it.rollback()
                    } catch (e: Throwable) {
                        throw e
                    }
                    throw e
                }
            }
        }
    }

    fun setConnectionPool(connectionPool: ResourcePool<Connection>) {
        this.connectionPool = connectionPool
    }
}
