package com.oneliang.ktx.frame.script

class FunctionResult(val result: Boolean = false,
                     val optimizeInputMap: Map<String, String>,
                     val functionResultItemMap: Map<String, FunctionResultItem>,
                     val functionResultItemMappingMap: Map<String, FunctionResultItem> = functionResultItemMap,
                     val totalFunctionResultItem: FunctionResultItem = FunctionResultItem())