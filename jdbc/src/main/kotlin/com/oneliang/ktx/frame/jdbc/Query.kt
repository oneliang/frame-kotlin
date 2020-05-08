package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

interface Query : BaseQuery {

    /**
     * use connection
     * @param block
     */
    @Throws(QueryException::class)
    fun <R> useConnection(block: (connection: Connection) -> R): R

    /**
     * use stable connection
     * @param block
     */
    @Throws(QueryException::class)
    fun <R> useStableConnection(block: (connection: Connection) -> R): R

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
    fun <T : Any> deleteObject(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

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
    fun <T : Any> deleteObjectNotById(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     * Method: delete class,by condition
     * @param <T>
     * @param kClass
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> deleteObject(kClass: KClass<T>, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     * Method: delete object collection,transaction,not sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> deleteObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

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
    fun <T : Any, M : Any> deleteObject(collection: Collection<T>, kClass: KClass<M>, table: String = Constants.String.BLANK): IntArray

    /**
     * Method: delete object by id,not sql binding
     * @param <T>
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> deleteObjectById(kClass: KClass<T>, id: IdType): Int

    /**
     * Method: delete object by multiple id,transaction,not sql binding
     * @param <T>
     * @param kClass
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> deleteObjectByIds(kClass: KClass<T>, ids: Array<IdType>): Int

    /**
     *
     * Method: insert object for sql binding
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> insertObject(instance: T, table: String = Constants.String.BLANK): Int

    /**
     * Method: insert object for sql binding and return the auto increment id
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> insertObjectForAutoIncrement(instance: T, table: String = Constants.String.BLANK): Int

    /**
     * Method: insert object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> insertObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

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
    fun <T : Any, M : Any> insertObject(collection: Collection<T>, kClass: KClass<M>, table: String = Constants.String.BLANK): IntArray

    /**
     * Method: update object,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> updateObject(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     * Method: update object not by id,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> updateObjectNotById(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     * Method: update object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> updateObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

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
    fun <T : Any, M : Any> updateObject(collection: Collection<T>, kClass: KClass<M>, table: String = Constants.String.BLANK): IntArray

    /**
     * Method: select object by id
     * @param <T>
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any, IdType : Any> selectObjectById(kClass: KClass<T>, id: IdType): T?

    /**
     * Method: select object list,by column,table,condition,parameters,it is sql binding
     * @param <T>
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectList(kClass: KClass<T>, selectColumns: Array<String> = emptyArray(), table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): List<T>

    /**
     * Method: select object list by sql,it is sql binding
     * @param <T>
     * @param kClass
     * @param sql
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectListBySql(kClass: KClass<T>, sql: String, parameters: Array<*> = emptyArray<Any>()): List<T>

    /**
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param kClass
     * @param page
     * @param countColumn
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectPaginationList(kClass: KClass<T>, page: Page, countColumn: String = Constants.String.BLANK, selectColumns: Array<String> = emptyArray(), table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): List<T>

    /**
     * Method: execute by sql ,for all sql,sql binding
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBySql(sql: String, parameters: Array<*> = emptyArray<Any>())

    /**
     * Method: execute query by sql statement,use caution,must close the statement
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeQueryBySql(sql: String, parameters: Array<*> = emptyArray<Any>()): ResultSet

    /**
     * Method: execute update
     * @param instance
     * @param table
     * @param executeType
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun <T : Any> executeUpdate(instance: T, table: String, condition: String, executeType: BaseQuery.ExecuteType): Int

    /**
     * Method: execute update by sql statement it is sql binding
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeUpdateBySql(sql: String, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     * Method: execute batch
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(sqls: Array<String>): IntArray

    /**
     * Method: execute batch,transaction
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(sql: String, parametersList: List<Array<*>>): IntArray

    /**
     * Method: execute batch
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray

    /**
     * Method: get the total size,it is sql binding
     * @param <T>
     * @param countColumn
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> totalRows(countColumn: String = Constants.String.BLANK, table: String, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     * Method: get the total size, it is sql binding
     * @param <T>
     * @param kClass
     * @param countColumn
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> totalRows(kClass: KClass<T>? = null, countColumn: String = Constants.String.BLANK, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<*> = emptyArray<Any>()): Int

    /**
     * execute transaction, if you need to stop transaction, you can throw exception
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeTransaction(transaction: Transaction): Boolean

    /**
     * execute transaction, if you need to stop transaction, you can throw exception
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeTransaction(transaction: () -> Boolean): Boolean
}
