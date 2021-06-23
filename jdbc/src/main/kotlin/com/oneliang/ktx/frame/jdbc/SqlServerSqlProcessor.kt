package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import java.util.*
import kotlin.reflect.KClass


/**
 * mostly for sql server database
 * @author Dandelion
 * @since 2011-01-07
 */
open class SqlServerSqlProcessor : DefaultSqlProcessor() {

    override val keywordSymbolLeft: String = Constants.Symbol.MIDDLE_BRACKET_LEFT
    override val keywordSymbolRight: String = Constants.Symbol.MIDDLE_BRACKET_RIGHT

}
