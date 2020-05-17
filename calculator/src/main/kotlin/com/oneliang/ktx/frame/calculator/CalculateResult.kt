package com.oneliang.ktx.frame.calculator

class CalculateResult(val result: Boolean = false,
                      val optimizeInputMap: Map<String, String>,
                      val calculateResultItemMap: Map<String, CalculateResultItem>,
                      val calculateResultItemMappingMap: Map<String, CalculateResultItem> = calculateResultItemMap,
                      val totalCalculateResultItem: CalculateResultItem = CalculateResultItem())