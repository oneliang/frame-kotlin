package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.reflect.KClass

open class BaseQueryImpl : BaseQuery {
    companion object {
        private val logger = LoggerManager.getLogger(BaseQueryImpl::class)
        private val DEFAULT_SQL_PROCESSOR = DefaultSqlProcessor()
    }

    private var sqlProcessor: SqlUtil.SqlProcessor = DEFAULT_SQL_PROCESSOR

    /**
     * Method: execute by sql,for all sql
     * @param connection
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBySql(connection: Connection, sql: String, parameters: Array<*>) {
        var preparedStatement: PreparedStatement? = null
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            val parameterString = parameters.joinToString()
            logger.info("%s, parameters:[%s]", parsedSql, parameterString)
            preparedStatement = connection.prepareStatement(parsedSql)
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            val begin = System.currentTimeMillis()
            preparedStatement.execute()
            logger.info("execute cost:%s, sql:%s, parameters:[%s]", (System.currentTimeMillis() - begin), parsedSql, parameterString)
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
    }

    /**
     * Through the class generate the sql
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
    override fun <T : Any> executeQuery(connection: Connection, kClass: KClass<T>, selectColumns: Array<String>, table: String, condition: String, parameters: Array<*>): List<T> {
        var resultSet: ResultSet? = null
        val list: List<T>
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val sql = SqlUtil.selectSql(selectColumns, table, condition, mappingBean)
            resultSet = this.executeQueryBySql(connection, sql, parameters)
            list = SqlUtil.resultSetToObjectList(resultSet, kClass, mappingBean, this.sqlProcessor)
            logger.debug("sql select result:%s, sql:%s, parameters:[%s]", list.size, sql, parameters.joinToString())
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
        return list
    }

    /**
     * Method: execute query by id
     * @param <T>
     * @param connection
     * @param kClass
     * @param id
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeQueryById(connection: Connection, kClass: KClass<T>, id: IdType): T? {
        var instance: T? = null
        val list: List<T> = executeQueryByIdOrIds(connection, kClass, arrayOf<Any>(id), true)
        if (list.isNotEmpty()) {
            instance = list[0]
        }
        return instance
    }

    /**
     * Method: execute query with ids
     * @param <T>
     * @param connection
     * @param kClass
     * @param ids
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeQueryByIds(connection: Connection, kClass: KClass<T>, ids: Array<IdType>): List<T> {
        return executeQueryByIdOrIds(connection, kClass, ids, false)
    }

    /**
     * Method: execute query with id or ids
     * @param <T>
     * @param connection
     * @param kClass
     * @param ids
     * @param singleId
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    private fun <T : Any, IdType : Any> executeQueryByIdOrIds(connection: Connection, kClass: KClass<T>, ids: Array<IdType>, singleId: Boolean): List<T> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val list: List<T>
        var resultSet: ResultSet? = null
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val sql: String
            if (singleId) {
                sql = SqlUtil.classToSelectSingleIdSql(kClass, mappingBean)
                resultSet = this.executeQueryBySql(connection, sql, ids)
            } else {
                sql = SqlUtil.classToSelectMultipleIdSql(kClass, ids, mappingBean)
                resultSet = this.executeQueryBySql(connection, sql)
            }
            list = SqlUtil.resultSetToObjectList(resultSet, kClass, mappingBean, this.sqlProcessor)
            logger.debug("sql select result:%s, sql:%s", list.size, sql)
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
        return list
    }

    /**
     * The base sql query
     * Method: execute query base on the connection and sql command
     * @param connection
     * @param kClass
     * @param sql
     * @param parameters
     * @return List
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeQueryBySql(connection: Connection, kClass: KClass<T>, sql: String, parameters: Array<*>): List<T> {
        var resultSet: ResultSet? = null
        val list: List<T>
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            resultSet = this.executeQueryBySql(connection, sql, parameters)
            list = SqlUtil.resultSetToObjectList(resultSet, kClass, mappingBean, this.sqlProcessor)
            logger.debug("sql select result:%s, sql:%s", list.size, sql)
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
        return list
    }

    /**
     * Method: execute query base on the connection and sql command
     * Caution: use this method must get Statement from the ResultSet and close it and close the ResultSet
     * @param connection
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeQueryBySql(connection: Connection, sql: String, parameters: Array<*>): ResultSet {
        val resultSet: ResultSet
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            val parameterString = parameters.joinToString()
            logger.info("%s, parameters:[%s]", parsedSql, parameterString)
            val preparedStatement = connection.prepareStatement(parsedSql)
            if (parameters.isNotEmpty()) {
                var index = 1
                for (parameter in parameters) {
                    this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                    index++
                }
            }
            val begin = System.currentTimeMillis()
            resultSet = preparedStatement.executeQuery()
            logger.info("execute cost:%s, sql:%s, parameters:[%s]", (System.currentTimeMillis() - begin), parsedSql, parameterString)
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return resultSet
    }

    /**
     * Method: execute query base on the connection and sql command for map data
     * @param connection
     * @param sql
     * @param columnDataKeyMap
     * @param columnClassMapping
     * @param parameters
     * @return List<Map<String, *>>
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeQueryBySqlForMap(connection: Connection, sql: String, columnDataKeyMap: Map<String, String>, columnClassMapping: Map<String, KClass<*>>, parameters: Array<*>): List<Map<String, *>> {
        val list = mutableListOf<Map<String, *>>()
        val resultSet = this.executeQueryBySql(connection, sql, parameters)
        resultSet.use {
            while (resultSet.next()) {
                val map = mutableMapOf<String, Any?>()
                columnDataKeyMap.forEach { (columnName, dataKey) ->
                    val kClass = columnClassMapping[columnName] ?: String::class
                    map[dataKey] = sqlProcessor.afterSelectProcess(kClass, it, columnName)
                }
                list += map
            }
        }
        return list
    }

    /**
     * Method: execute insert
     * @param connection
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeInsert(connection: Connection, instance: T, table: String): Int {
        return this.executeUpdate(connection, instance, table, condition = Constants.String.BLANK, executeType = BaseQuery.ExecuteType.INSERT)
    }

    /**
     * Method: execute insert for auto increment and return the auto increment id
     * @param connection
     * @param <T>
     * @param table
     * @return int for id
     * @throws QueryException
    </T> */
    override fun <T : Any> executeInsertForAutoIncrement(connection: Connection, instance: T, table: String): Int {
        val id: Int
        try {
            val kClass = instance::class
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val (sql, parameterList) = SqlInjectUtil.objectToInsertSql(instance, table, mappingBean)
            id = this.executeInsertForAutoIncrementBySql(connection, sql, parameterList.toTypedArray())
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return id
    }

    /**
     * Method: execute insert collection(list),transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeInsert(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     * Method: execute update
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(connection: Connection, instance: T, table: String, updateFields: Array<String>, condition: String): Int {
        return this.executeUpdate(connection, instance, table, updateFields, condition, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     * Method: execute update collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     * Method: execute delete with id
     * @param <T>
     * @param connection
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeDeleteById(connection: Connection, kClass: KClass<T>, id: IdType): Int {
        val sql: String
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            sql = SqlUtil.classToDeleteSingleRowSql(kClass, id, mappingBean)
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return executeUpdateBySql(connection, sql)
    }

    /**
     * Method: execute delete with multi id,transaction
     * @param <T>
     * @param connection
     * @param kClass
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeDeleteByIds(connection: Connection, kClass: KClass<T>, ids: Array<IdType>): Int {
        if (ids.isEmpty()) {
            return 0
        }
        val updateResult: Int
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val sqlPair = SqlInjectUtil.classToDeleteSql(kClass, byId = true, mappingBean = mappingBean)
            val parametersList = ids.map { arrayOf<Any>(it) }
            updateResult = this.executeBatch(connection, sqlPair.first, parametersList).size
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return updateResult
    }

    /**
     * Method: execute delete,condition of auto generate include by id
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeDelete(connection: Connection, instance: T, table: String, condition: String): Int {
        return this.executeUpdate(connection, instance, table, condition = condition, executeType = BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
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
    override fun <T : Any> executeDelete(connection: Connection, kClass: KClass<T>, condition: String, parameters: Array<*>): Int {
        val result: Int
        try {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val sql = SqlUtil.deleteSql(Constants.String.BLANK, condition, mappingBean)
            result = this.executeUpdateBySql(connection, sql, parameters)
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return result
    }

    /**
     * Method: execute delete collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeDelete(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     * Method: execute insert for auto increment by sql and return the auto increment id
     * @param connection
     * @param sql
     * @param parameters
     * @return int id
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeInsertForAutoIncrementBySql(connection: Connection, sql: String, parameters: Array<*>): Int {
        var preparedStatement: PreparedStatement? = null
        var id = 0
        var resultSet: ResultSet? = null
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            val parameterString = parameters.joinToString()
            logger.info("%s, parameters:[%s]", parsedSql, parameterString)
            preparedStatement = connection.prepareStatement(parsedSql, Statement.RETURN_GENERATED_KEYS)
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            val begin = System.currentTimeMillis()
            preparedStatement!!.execute()
            logger.info("execute cost:%s, sql:%s, parameters:[%s]", (System.currentTimeMillis() - begin), parsedSql, parameterString)
            resultSet = preparedStatement.generatedKeys
            if (resultSet != null && resultSet.next()) {
                id = resultSet.getInt(1)
            }
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
        return id
    }

    /**
     * Method: execute update include insert sql and update sql,for sql binding
     * @param connection
     * @param instance
     * @param updateFields, only for update
     * @param table
     * @param executeType
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(connection: Connection, instance: T, table: String, updateFields: Array<String> = emptyArray(), condition: String, executeType: BaseQuery.ExecuteType): Int {
        val rows: Int
        try {
            val kClass = instance::class
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
            val (sql, parameterList) = when (executeType) {
                BaseQuery.ExecuteType.INSERT -> SqlInjectUtil.objectToInsertSql(instance, table, mappingBean)
                BaseQuery.ExecuteType.UPDATE_BY_ID -> SqlInjectUtil.objectToUpdateSql(instance, updateFields, table, condition, true, mappingBean)
                BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> SqlInjectUtil.objectToUpdateSql(instance, updateFields, table, condition, false, mappingBean)
                BaseQuery.ExecuteType.DELETE_BY_ID -> SqlInjectUtil.objectToDeleteSql(instance, table, condition, true, mappingBean)
                BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> SqlInjectUtil.objectToDeleteSql(instance, table, condition, false, mappingBean)
            }
            rows = this.executeUpdateBySql(connection, sql, parameterList.toTypedArray())
        } catch (e: Throwable) {
            throw QueryException(e)
        }

        return rows
    }

    /**
     * Method: execute update collection,transaction not for sql binding
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(connection: Connection, collection: Collection<T>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        var rows = IntArray(0)
        if (collection.isNotEmpty()) {
            try {
                val sqls = Array(collection.size) { Constants.String.BLANK }
                for ((i, instance) in collection.withIndex()) {
                    val kClass = instance::class
                    val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
                    when (executeType) {
                        BaseQuery.ExecuteType.INSERT -> sqls[i] = SqlUtil.objectToInsertSql(instance, table, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.UPDATE_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, table, Constants.String.BLANK, true, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, table, Constants.String.BLANK, false, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.DELETE_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, table, Constants.String.BLANK, true, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, table, Constants.String.BLANK, false, mappingBean, this.sqlProcessor)
                    }
                }
                rows = this.executeBatch(connection, sqls)
            } catch (e: Throwable) {
                throw QueryException(e)
            }
        }
        return rows
    }

    /**
     * use batch
     */
    private fun <R> useBatch(connection: Connection, block: () -> R): R {
        val customTransaction = TransactionManager.isCustomTransaction()
        return try {
            if (!customTransaction) {
                connection.autoCommit = false
            }
            val result = block()
            if (!customTransaction) {
                connection.commit()
            }
            result
        } catch (e: Throwable) {
            if (!customTransaction) {
                try {
                    connection.rollback()
                } catch (ex: Throwable) {
                    throw QueryException(ex)
                }
            }
            throw QueryException(e)
        } finally {
            try {
                if (!customTransaction) {
                    connection.autoCommit = true
                }
            } catch (e: Throwable) {
                throw QueryException(e)
            }
        }
    }

    /**
     * Method: execute update collection,transaction,for preparedStatement sql binding
     * @param <T>
     * @param <M>
     * @param connection
     * @param collection
     * @param kClass mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    protected fun <T : Any, M : Any> executeUpdate(connection: Connection, collection: Collection<T>, kClass: KClass<M>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        if (collection.isEmpty()) {
            logger.warning("collection is empty, class:$kClass")
            return IntArray(0)
        }
        return useBatch(connection) {
            val rows: IntArray
            var preparedStatement: PreparedStatement? = null
            try {
                val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
                val (sql, fieldNameList) = when (executeType) {
                    BaseQuery.ExecuteType.INSERT -> SqlInjectUtil.classToInsertSql(kClass, table, mappingBean)
                    BaseQuery.ExecuteType.UPDATE_BY_ID -> SqlInjectUtil.classToUpdateSql(kClass, table, Constants.String.BLANK, true, mappingBean)
                    BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> SqlInjectUtil.classToUpdateSql(kClass, table, Constants.String.BLANK, false, mappingBean)
                    BaseQuery.ExecuteType.DELETE_BY_ID -> SqlInjectUtil.classToDeleteSql(kClass, table, Constants.String.BLANK, true, mappingBean)
                    BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> SqlInjectUtil.classToDeleteSql(kClass, table, Constants.String.BLANK, false, mappingBean)
                }
                val parsedSql = DatabaseMappingUtil.parseSql(sql)
                logger.info(parsedSql)
                preparedStatement = connection.prepareStatement(parsedSql)
                for (instance in collection) {
                    var index = 1
                    for (fieldName in fieldNameList) {
                        val parameter = ObjectUtil.getterOrIsMethodInvoke(instance, fieldName)
                        this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                        index++
                    }
                    preparedStatement.addBatch()
                }
                val begin = System.currentTimeMillis()
                rows = preparedStatement.executeBatch()
                preparedStatement.clearBatch()
                logger.info("execute cost:%s, sql execute batch result:%s, sql:%s", (System.currentTimeMillis() - begin), rows.size, parsedSql)
                rows
            } finally {
                preparedStatement?.close()
            }
        }
    }

    /**
     * Method: execute update by sql statement
     * @param connection
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeUpdateBySql(connection: Connection, sql: String, parameters: Array<*>): Int {
        var preparedStatement: PreparedStatement? = null
        val updateResult: Int
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            val parameterString = parameters.joinToString()
            logger.info("%s, parameters:[%s]", parsedSql, parameterString)
            preparedStatement = connection.prepareStatement(parsedSql)!!
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            val begin = System.currentTimeMillis()
            updateResult = preparedStatement.executeUpdate()
            logger.info("execute cost:%s, sql update result:%s, sql:%s, parameters:[%s]", (System.currentTimeMillis() - begin), updateResult, parsedSql, parameterString)
        } catch (e: Throwable) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
            }
        }
        return updateResult
    }

    /**
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, sqls: Array<String>): IntArray {
        return executeBatch(connection, sqls.toList())
    }

    /**
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sqlList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, sqlList: List<String>): IntArray {
        if (sqlList.isEmpty()) {
            return IntArray(0)
        }
        return useBatch(connection) {
            val results: IntArray
            var statement: Statement? = null
            try {
                statement = connection.createStatement()
                for (sql in sqlList) {
                    val parsedSql = DatabaseMappingUtil.parseSql(sql)
                    logger.info(parsedSql)
                    statement.addBatch(parsedSql)
                }
                val begin = System.currentTimeMillis()
                results = statement.executeBatch()
                logger.info("execute cost:%s, batch result:%s", (System.currentTimeMillis() - begin), results.size)
                statement.clearBatch()
                results
            } finally {
                statement?.close()
            }
        }
    }

    /**
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, sql: String, parametersList: List<Array<*>>): IntArray {
        if (parametersList.isEmpty()) {
            return IntArray(0)
        }
        return useBatch(connection) {
            val results: IntArray
            var preparedStatement: PreparedStatement? = null
            try {
                val parsedSql = DatabaseMappingUtil.parseSql(sql)
                logger.info(parsedSql)
                preparedStatement = connection.prepareStatement(parsedSql)
                for (parameters in parametersList) {
                    var index = 1
                    for (parameter in parameters) {
                        this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                        index++
                    }
                    preparedStatement.addBatch()
                }
                val begin = System.currentTimeMillis()
                results = preparedStatement.executeBatch()
                logger.info("execute cost:%s, batch result:%s", (System.currentTimeMillis() - begin), results.size)
                preparedStatement.clearBatch()
                results
            } finally {
                preparedStatement?.close()
            }
        }
    }

    /**
     * Method: execute batch by connection,transaction
     * @param connection
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray {
        if (batchObjectCollection.isEmpty()) {
            return IntArray(0)
        }
        val results: IntArray
        try {
            val sqls = Array(batchObjectCollection.size) { Constants.String.BLANK }
            for ((i, batchObject) in batchObjectCollection.withIndex()) {
                val instance = batchObject.instance
                val executeType = batchObject.executeType
                val condition = batchObject.condition
                val kClass = instance.javaClass
                val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass.kotlin) ?: throw MappingNotFoundException("Mapping is not found, class:$kClass")
                when (executeType) {
                    BaseQuery.ExecuteType.INSERT -> sqls[i] = SqlUtil.objectToInsertSql(instance, Constants.String.BLANK, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.UPDATE_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, Constants.String.BLANK, condition, true, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, Constants.String.BLANK, condition, false, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.DELETE_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, Constants.String.BLANK, condition, true, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, Constants.String.BLANK, condition, false, mappingBean, this.sqlProcessor)
                }
            }
            results = this.executeBatch(connection, sqls)
        } catch (e: Throwable) {
            throw QueryException(e)
        }
        return results
    }

    /**
     * @param sqlProcessor the sqlProcessor to set
     */
    fun setSqlProcessor(sqlProcessor: SqlUtil.SqlProcessor) {
        this.sqlProcessor = sqlProcessor
    }
}
