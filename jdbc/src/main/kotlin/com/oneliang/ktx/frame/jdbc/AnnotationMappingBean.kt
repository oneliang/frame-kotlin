package com.oneliang.ktx.frame.jdbc

import java.util.Arrays


class AnnotationMappingBean : MappingBean() {

    /**
     * @return the condition
     */
    /**
     * @param condition the condition to set
     */
    var condition: String? = null
    /**
     * @return the dropIfExist
     */
    /**
     * @param dropIfExist the dropIfExist to set
     */
    var isDropIfExist = false
    /**
     * @return the createTableSqls
     */
    /**
     * @param createTableSqls the createTableSqls to set
     */
    var createTableSqls: Array<String>? = null
        set(createTableSqls) {
            if (createTableSqls != null) {
                field = Arrays.copyOf(createTableSqls, createTableSqls.size)
            }
        }
}
